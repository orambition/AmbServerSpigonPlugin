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

        int slotCount = (int) (9 * (1 +Math.ceil(publicTeleporters.size()/9D)+ Math.ceil((onlinePlayers.size()-1)/9D) + Math.ceil(privateTeleporters.size()/9D)));
        Inventory inventory = Bukkit.createInventory(null, slotCount, PluginConfig.tpBookMenuTitle);
        int num = 0;
        // 公共地址
        for (Telepoter telepoter : publicTeleporters) {
            inventory.setItem(num++, buildMenuItem(telepoter.getItemType(), telepoter.getName(), telepoter.getNum(), telepoter.getItemLore()));
        } num = (int) (Math.ceil(num/9D) * 9);
        // 在线玩家
        for (Player p : onlinePlayers) {
            if (!p.getUniqueId().toString().equals(uuid)){
                inventory.setItem(num++, buildMenuItem(p));
            }
        } num = (int) (Math.ceil(num/9D) * 9);
        // 私人地点
        for (Telepoter telepoter : privateTeleporters) {
            inventory.setItem(num++, buildMenuItem(telepoter.getItemType(), telepoter.getName(), telepoter.getNum(), telepoter.getItemLore()));
        } num = (int) (Math.ceil(num/9D) * 9);
        // 传送开关
        inventory.setItem(num++, buildSwitchMenuItem(switchTeleporter));
        // 死亡地点
        for (Telepoter telepoter : deadTeleporters) {
            inventory.setItem(num++, buildMenuItem(telepoter.getItemType(), telepoter.getName(), telepoter.getNum(), telepoter.getItemLore()));
        }
        num += 8 - num % 9;
        // 新增私人地点
        inventory.setItem(num, buildAddMenuItem((int) Math.pow(tpBookAddTpPrice,privateTeleporters.size()+1)));
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
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "点击传送至此玩家");
        lore.add(ChatColor.RESET +""+ ChatColor.RED +"此操作将花费:");
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD +"- ["+ tpBookCurrencyItemName + "x" +tpBookAddTpPrice +"] 或:");
        lore.add(ChatColor.GREEN + "- ["+tpBookAddTpPrice+"页]传送书");

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
        itemMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "是否允许其他人传送到身边");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "当前设置为:");
        if (0 == tpSwitch) {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "不允许任何人传送到身边");
            lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "点击开启位置共享");
            lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "允许其他人传送到身边");
        } else {
            if (1 == tpSwitch) {
                lore.add(ChatColor.RESET + "" + ChatColor.RED + "允许任何人传送到身边");
                lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "点击切换设置");
            } else {
                lore.add(ChatColor.RESET + "" + ChatColor.RED + "传送前发出申请");
                lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "点击切换为禁止传送");
            }
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack buildAddMenuItem(int price) {
        ItemStack itemStack = new ItemStack(addPrivateTpItem, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "添加传送点");
        itemMeta.addEnchant(Enchantment.DAMAGE_ALL, price, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "点击将所在位置标记为传送点");
        lore.add(ChatColor.RESET +""+ ChatColor.RED +"此操作将花费:");
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "- ["+tpBookCurrencyItemName + "x" +price+"]");
        if (price < 10){
            lore.add(ChatColor.GREEN + "或:["+price*100+"点经验值]");
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
