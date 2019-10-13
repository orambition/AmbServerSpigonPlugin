package amb.server.plugin.service.tpb;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.model.Telepoter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static amb.server.plugin.config.PluginConfig.*;
import static amb.server.plugin.service.tpb.TpBookDataService.*;

public class TpBookGUI {

    public static void openMenu(Player player) {
        player.openInventory(getInventoryMenu(player.getUniqueId().toString()));
    }

    private static Inventory getInventoryMenu(String uuid) {
        List<Telepoter> publicTeleporters = getAllPublicTeleporter();
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        List<Telepoter> privateTeleporters = getPlayerPrivateTeleporter(uuid);
        List<Telepoter> deadTeleporters = getPlayerDeadTeleporter(uuid);
        int switchTeleporter = TpBookService.getTeleporterSwitch(uuid);

        int slotCount = (int) (9 * (1 + Math.ceil(publicTeleporters.size() / 9D) + Math.ceil((onlinePlayers.size() - 1) / 9D) + Math.ceil(privateTeleporters.size() / 9D)));
        Inventory inventory = Bukkit.createInventory(null, slotCount, PluginConfig.tpBookMenuTitle);
        int num = 0;
        // ������ַ
        for (Telepoter telepoter : publicTeleporters) {
            inventory.setItem(num++, buildMenuItem(telepoter.getItemType(), telepoter.getName(), telepoter.getNum(), telepoter.getItemLore()));
        }
        num = (int) (Math.ceil(num / 9D) * 9);
        // �������
        for (Player p : onlinePlayers) {
            if (!p.getUniqueId().toString().equals(uuid)) {
                inventory.setItem(num++, buildMenuItem(p));
            }
        }
        num = (int) (Math.ceil(num / 9D) * 9);
        // ˽�˵ص�
        for (Telepoter telepoter : privateTeleporters) {
            inventory.setItem(num++, buildMenuItem(telepoter.getItemType(), telepoter.getName(), telepoter.getNum(), telepoter.getItemLore()));
        }
        num = (int) (Math.ceil(num / 9D) * 9);
        // ���Ϳ���
        inventory.setItem(num++, buildSwitchMenuItem(switchTeleporter));
        // �����ص�
        if (deadTeleporters.size() == 0) {
            inventory.setItem(num++, buildDeadInfoMenuItem());
        } else {
            for (Telepoter telepoter : deadTeleporters) {
                inventory.setItem(num++, buildMenuItem(telepoter.getItemType(), telepoter.getName(), telepoter.getNum(), telepoter.getItemLore()));
            }
        }
        num += (deadTpMax+1) - num % 9;
        // ǰһ���͵�
        Telepoter beforeTelepoter = TpBookDataService.getBeforeTeleporter(uuid);
        if (beforeTelepoter != null) {
            inventory.setItem(num, buildMenuItem(beforeTelepoter.getItemType(), beforeTelepoter.getName(), null, beforeTelepoter.getItemLore()));
        }num++;
        // ���ٴ��͵�
        inventory.setItem(num++, buildFastInfoMenuItem(uuid));
        // ����˽�˵ص�
        inventory.setItem(num, buildAddMenuItem((int) Math.pow(tpBookAddTpPrice, privateTeleporters.size() + 1)));
        return inventory;
    }

    private static ItemStack buildMenuItem(Material material, String name, String num, List<String> lore) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + name);
        itemMeta.setLore(lore);
        if (StringUtils.isNotBlank(num)) {
            itemMeta.addEnchant(Enchantment.DAMAGE_ALL, Integer.parseInt(num), true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack buildMenuItem(Player player) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + player.getDisplayName());
        itemMeta.setOwningPlayer(player);
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "��������������");
        lore.add(ChatColor.RESET + "" + ChatColor.RED + "�˲���������:");
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "- [" + tpBookCurrencyItemName + "x" + tpBookAddTpPrice + "] ��:");
        lore.add(ChatColor.GREEN + "- [" + tpBookAddTpPrice + "ҳ]������");

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack buildSwitchMenuItem(int tpSwitch) {
        ItemStack itemStack;
        if (0 == tpSwitch) {
            itemStack = new ItemStack(switchOffTpItem, 1);
        } else {
            itemStack = new ItemStack(switchTpItem, 1);
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "�Ƿ����������˴��͵����");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "��ǰ����Ϊ:");
        if (0 == tpSwitch) {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "�������κ��˴��͵����");
            lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "�������λ�ù���");
            lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "���������˴��͵����");
        } else {
            if (1 == tpSwitch) {
                lore.add(ChatColor.RESET + "" + ChatColor.RED + "�����κ��˴��͵����");
                lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "����л�����");
            } else {
                lore.add(ChatColor.RESET + "" + ChatColor.RED + "����ǰ��������");
                lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "����л�Ϊ��ֹ����");
            }
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack buildAddMenuItem(int price) {
        ItemStack itemStack = new ItemStack(addPrivateTpItem, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "��Ӵ��͵�");
        itemMeta.addEnchant(Enchantment.DAMAGE_ALL, price, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "���������λ�ñ��Ϊ���͵�");
        lore.add(ChatColor.RESET + "" + ChatColor.RED + "�˲���������:");
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "- [" + tpBookCurrencyItemName + "x" + price + "]");
        if (price < 10) {
            lore.add(ChatColor.GREEN + "��:[" + price * 100 + "�㾭��ֵ]");
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack buildDeadInfoMenuItem() {
        ItemStack itemStack = new ItemStack(deadInfoItem, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "����ǰԵ");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "Я������������ʱ");
        lore.add(ChatColor.RESET + "�����ص�ᱻ��¼");
        lore.add(ChatColor.RESET + "" + ChatColor.RED + "������һ��������!");
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "�������������:");
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "[һ��] ���ͻ���!");

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * ���ÿ��ٴ��͵�˵�
     * @param uuid
     * @return
     */
    private static ItemStack buildFastInfoMenuItem(String uuid) {
        ItemStack itemStack = new ItemStack(privateFastTpItem, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "���ٴ��͵�");
        List<String> lore = new ArrayList<String>();

        Telepoter telepoter = getPrivateFastTeleporter(uuid);
        if (null != telepoter){
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "��ǰ�ص�Ϊ:");
            lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "["+telepoter.getName()+"]");
            lore.add(ChatColor.RESET + "��[�ֳִ�����]ʱ");
            lore.add(ChatColor.RESET + "��[Ǳ��״̬]ʹ�ô�����");
            lore.add(ChatColor.RESET + "�ɿ��ٴ������˵ص�");
            lore.add(ChatColor.RESET +  "" + ChatColor.RED + "�˲���ͬ�����ķ���");
        }else {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "[��ǰ�ص�Ϊ��]");
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "��[������˵�]ʹ��");
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "[Shift+������]");
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "�����õص�Ϊ���ٴ��͵�");
        }

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static void openBook(Player player) {
        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
        bookMeta.setTitle(PluginConfig.tpBookMenuTitle);
        bookMeta.setAuthor("Mr_Amb");
        List<String> pages = new ArrayList<>();

        pages.add("��l   ��������ʹ���ֲᡷ��r\n" +
                "1.�ֳִ����鰴��[ʹ����Ʒ]�ɴ򿪴��ֲ�;\n" +
                "2.�ֳִ����鰴��[������]�ɴ򿪡�2��l���Ͳ˵���0;\n" +
                "3.�ֳִ�������[��c��lǱ�С�0]״̬\n" +
                "����[������ť]�ɽ��С�d��l���ٴ��͡�0;��0"); // Page 1
        pages.add("2.1��2��l���Ͳ˵���0�л�����չʾ:\n" +
                "��2��l�������͵��0,��2��l������ҡ�0,��2��l˽�˴��͵��0,��2��l�����ص��0\n" +
                "�ȡ�4��l����ѡ���0;\n" +
                "����󽫴��͵���Ӧ�ص�,ÿ�δ��;���Ҫ����[��Ʒ],�������в���������Ʒʱ,�����Ĵ�������д���;\n" +
                "2.2��2��l���Ͳ˵���0�ײ�������:\n" +
                "��6��l�����Ϳ��ء�0,��6��l֮ǰ�ص��0,��6��l���ٴ��͵��0,��6��l���˽�˴��͵��0\n" +
                "�ȡ�4��l����ѡ���0;\n" +
                "������ִ����Ӧ����;��0"); // Page 2
        pages.add("-��2��l�������͵��0:\n" +
                "�ɹ���Ա��ӵĴ��͵�,�������˿ɼ�;\n" +
                "-��2��l������ҡ�0:\n" +
                "��ǰ���ߵ��������;\n" +
                "-��2��l˽�˴��͵��0:\n" +
                "���Լ���ӵĴ��͵�,�����Լ��ɼ�,ʹ��[Shift+�Ҽ�]����ɽ���ɾ��,ɾ�����᷵���κ���Ʒ,���������;\n" +
                "-��2��l�����ص��0:\n" +
                "Я������������ʱ,���Զ���¼�����ص�,��������һ��������;��0"); // Page 3
        pages.add("-��6��l�����Ϳ��ء�0:\n" +
                "�Ƿ�����������Ҵ��͵��㵱ǰ��λ��,Ĭ��Ϊ������;\n" +
                "-��6��l֮ǰ�ص��0:\n" +
                "ÿ��ʹ�ô�������д���ʱ,���Զ���¼����ǰ��λ��,���ڻع���ǰһ�ص�;\n" +
                "-��6��l���˽�˴��͵��0:\n" +
                "�������ӵ�ǰ�ص�Ϊ��2��l˽�˴��͵��0,�˲���������һ��[��Ʒ],��������˽�˴��͵�����������,���ĵ���ƷҲ��֮����;��0");
        pages.add("-��d��l���ٴ��͡�0:\n" +
                "���ڲ��򿪴��Ͳ˵�������½��д���,��Ҫ�������ÿ��ٴ��͵�,���ܽ���ʹ��;\n" +
                "-���ٴ��͵��ͨ���ڡ�2��l���Ͳ˵���0��,\n" +
                "ʹ��[Shift+���]���\n" +
                "[����/˽�˴��͵�],���ɽ�����Ϊ���ٴ��͵�,��ʹ�ô��͵㱻ɾ��,���ٴ�����Ȼ����;��0");
        bookMeta.setPages(pages);
        writtenBook.setItemMeta(bookMeta);
        player.openBook(writtenBook);
    }
}
