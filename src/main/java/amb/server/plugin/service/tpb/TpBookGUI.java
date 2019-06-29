package amb.server.plugin.service.tpb;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.model.Telepoter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static amb.server.plugin.config.PluginConfig.*;
import static amb.server.plugin.service.tpb.TpBookDataService.*;

public class TpBookGUI {

    public static void openBook(Player player) {
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
        num += 7 - num % 9;
        // ˽�˿��ٴ��͵�
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
        if (null != num) {
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
            lore.add(ChatColor.RESET + "��[Ǳ��״̬]�򿪴�����");
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
}
