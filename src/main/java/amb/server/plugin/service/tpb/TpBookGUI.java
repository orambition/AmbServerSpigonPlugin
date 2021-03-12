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
        // 公共地址
        for (Telepoter telepoter : publicTeleporters) {
            inventory.setItem(num++, buildMenuItem(telepoter.getItemType(), telepoter.getName(), telepoter.getNum(), telepoter.getItemLore()));
        }
        num = (int) (Math.ceil(num / 9D) * 9);
        // 在线玩家
        for (Player p : onlinePlayers) {
            if (!p.getUniqueId().toString().equals(uuid)) {
                inventory.setItem(num++, buildMenuItem(p));
            }
        }
        num = (int) (Math.ceil(num / 9D) * 9);
        // 私人地点
        for (Telepoter telepoter : privateTeleporters) {
            inventory.setItem(num++, buildMenuItem(telepoter.getItemType(), telepoter.getName(), telepoter.getNum(), telepoter.getItemLore()));
        }
        num = (int) (Math.ceil(num / 9D) * 9);
        // 传送开关
        inventory.setItem(num++, buildSwitchMenuItem(switchTeleporter));
        // 死亡地点
        if (deadTeleporters.size() == 0) {
            inventory.setItem(num++, buildDeadInfoMenuItem());
        } else {
            for (Telepoter telepoter : deadTeleporters) {
                inventory.setItem(num++, buildMenuItem(telepoter.getItemType(), telepoter.getName(), telepoter.getNum(), telepoter.getItemLore()));
            }
        }
        num += (deadTpMax+1) - num % 9;
        // 前一传送点
        Telepoter beforeTelepoter = TpBookDataService.getBeforeTeleporter(uuid);
        if (beforeTelepoter != null) {
            inventory.setItem(num, buildMenuItem(beforeTelepoter.getItemType(), beforeTelepoter.getName(), null, beforeTelepoter.getItemLore()));
        }num++;
        // 快速传送点
        inventory.setItem(num++, buildFastInfoMenuItem(uuid));
        // 新增私人地点
        inventory.setItem(num, buildAddMenuItem((int) Math.pow(tpBookAddTpPrice, privateTeleporters.size() + 1)));
        return inventory;
    }

    private static ItemStack buildMenuItem(Material material, String name, String num, List<String> lore) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GOLD + name);
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
        itemMeta.setDisplayName(ChatColor.GOLD + player.getDisplayName());
        itemMeta.setOwningPlayer(player);
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "点击传送至此玩家");
        lore.add(ChatColor.RED + "此操作将花费:");
        lore.add(ChatColor.GOLD + "- [" + tpBookCurrencyItemName + "x" + tpBookAddTpPrice + "] 或:");
        lore.add(ChatColor.GREEN + "- [" + tpBookAddTpPrice + "页]传送书");

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
        itemMeta.setDisplayName(ChatColor.GOLD + "是否允许其他人传送到身边");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "当前设置为:");
        if (0 == tpSwitch) {
            lore.add(ChatColor.RED + "不允许任何人传送到身边");
            lore.add(ChatColor.GOLD + "点击开启位置共享");
            lore.add(ChatColor.GOLD + "允许其他人传送到身边");
        } else {
            if (1 == tpSwitch) {
                lore.add(ChatColor.RED + "允许任何人传送到身边");
                lore.add(ChatColor.GOLD + "点击切换设置");
            } else {
                lore.add(ChatColor.RED + "传送前发出申请");
                lore.add(ChatColor.GOLD + "点击切换为禁止传送");
            }
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack buildAddMenuItem(int price) {
        ItemStack itemStack = new ItemStack(addPrivateTpItem, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GOLD + "添加传送点");
        itemMeta.addEnchant(Enchantment.DAMAGE_ALL, price, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "点击将所在位置标记为传送点");
        lore.add(ChatColor.RED + "此操作将花费:");
        lore.add(ChatColor.GOLD + "- [" + tpBookCurrencyItemName + "x" + price + "]");
        if (price < 10) {
            lore.add(ChatColor.GREEN + "或:[" + price * 100 + "点经验值]");
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack buildDeadInfoMenuItem() {
        ItemStack itemStack = new ItemStack(deadInfoItem, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GOLD + "再续前缘");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "携带传送书死亡时");
        lore.add(ChatColor.RESET + "死亡地点会被记录");
        lore.add(ChatColor.RED + "并消耗一本传送书!");
        lore.add(ChatColor.GOLD + "复活后会立即获得:");
        lore.add(ChatColor.GOLD + "[一次] 传送机会!");

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * 设置快速传送点菜单
     * @param uuid
     * @return
     */
    private static ItemStack buildFastInfoMenuItem(String uuid) {
        ItemStack itemStack = new ItemStack(privateFastTpItem, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GOLD + "快速传送点");
        List<String> lore = new ArrayList<String>();

        Telepoter telepoter = getPrivateFastTeleporter(uuid);
        if (null != telepoter){
            lore.add(ChatColor.GREEN + "当前地点为:");
            lore.add(ChatColor.GOLD + "["+telepoter.getName()+"]");
            lore.add(ChatColor.RESET + "当[手持传送书]时");
            lore.add(ChatColor.RESET + "以[潜行状态]使用传送书");
            lore.add(ChatColor.RESET + "可快速传送至此地点");
            lore.add(ChatColor.RESET +  "" + ChatColor.RED + "此操作同样消耗费用");
        }else {
            lore.add(ChatColor.RED + "[当前地点为空]");
            lore.add(ChatColor.GREEN + "在[传送书菜单]使用");
            lore.add(ChatColor.GREEN + "[Shift+鼠标左键]");
            lore.add(ChatColor.GREEN + "可设置地点为快速传送点");
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

        pages.add("§l   《传送书使用手册》§r\n" +
                "1.手持传送书按下[使用物品]可打开此手册;\n" +
                "2.手持传送书按下[攻击键]可打开§2§l传送菜单§0;\n" +
                "3.手持传送书在[§c§l潜行§0]状态\n" +
                "按下[攻击按钮]可进行§d§l快速传送§0;§0"); // Page 1
        pages.add("2.1§2§l传送菜单§0中会依次展示:\n" +
                "§2§l公共传送点§0,§2§l在线玩家§0,§2§l私人传送点§0,§2§l死亡地点§0\n" +
                "等§4§l传送选项§0;\n" +
                "点击后将传送到相应地点,每次传送均需要消耗[物品],当背包中不含所需物品时,将消耗传送书进行传送;\n" +
                "2.2§2§l传送菜单§0底部还包含:\n" +
                "§6§l被传送开关§0,§6§l之前地点§0,§6§l快速传送点§0,§6§l添加私人传送点§0\n" +
                "等§4§l功能选项§0;\n" +
                "点击后可执行相应功能;§0"); // Page 2
        pages.add("-§2§l公共传送点§0:\n" +
                "由管理员添加的传送点,对所有人可见;\n" +
                "-§2§l在线玩家§0:\n" +
                "当前在线的所有玩家;\n" +
                "-§2§l私人传送点§0:\n" +
                "你自己添加的传送点,仅你自己可见,使用[Shift+右键]点击可将其删除,删除不会返还任何物品,请谨慎操作;\n" +
                "-§2§l死亡地点§0:\n" +
                "携带传送书死亡时,会自动记录死亡地点,但将消耗一本传送书;§0"); // Page 3
        pages.add("-§6§l被传送开关§0:\n" +
                "是否允许其他玩家传送到你当前的位置,默认为不允许;\n" +
                "-§6§l之前地点§0:\n" +
                "每次使用传送书进行传送时,会自动记录传送前的位置,用于回滚到前一地点;\n" +
                "-§6§l添加私人传送点§0:\n" +
                "点击后将添加当前地点为§2§l私人传送点§0,此操作将消耗一定[物品],并且随着私人传送点数量的增加,消耗的物品也随之增加;§0");
        pages.add("-§d§l快速传送§0:\n" +
                "可在不打开传送菜单的情况下进行传送,需要事先设置快速传送点,才能进行使用;\n" +
                "-快速传送点可通过在§2§l传送菜单§0中,\n" +
                "使用[Shift+左键]点击\n" +
                "[公共/私人传送点],即可将其标记为快速传送点,即使该传送点被删除,快速传送依然可用;§0");
        bookMeta.setPages(pages);
        writtenBook.setItemMeta(bookMeta);
        player.openBook(writtenBook);
    }
}
