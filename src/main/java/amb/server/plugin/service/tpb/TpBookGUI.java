package amb.server.plugin.service.tpb;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.model.Telepoter;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TpBookGUI {

    public static void openBook(Player player){
        player.sendMessage("openbook");
        player.openInventory(getInventoryMenu(player));
    }

    private static Inventory getInventoryMenu(Player player){
        List<Telepoter> publicTeleporters = TpBookService.getAllPublicTeleporter();
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        List<Telepoter> privateTeleporters = TpBookService.getPlayerTeleporter(player.getUniqueId().toString());

        Inventory inventory = Bukkit.createInventory(null, 9*(publicTeleporters.size()/9 + onlinePlayers.size()/9 + privateTeleporters.size()/9 + 4), PluginConfig.tpBookMenuTitle);
        int num = 0;
        // 公共地址
        for (Telepoter telepoter : publicTeleporters){
            inventory.setItem(num++, buildMenuItem(PluginConfig.publicTpItem,telepoter.getName(),"创建者:"+telepoter.getAuthor()));
        }num+=9-num%9;
        // 在线玩家
        for (Player p : onlinePlayers){
            inventory.setItem(num++, buildMenuItem(p));
        }num+=9-num%9;
        // 私人地点
        for (Telepoter telepoter : privateTeleporters){
            inventory.setItem(num++, buildMenuItem(PluginConfig.privateTpItem,telepoter.getName(),telepoter.getWorld(),telepoter.getXYZ()));
        }num+=9-num%9;
        // 传送开关
        inventory.setItem(num++,buildMenuItem(Material.APPLE,"传送开关","点击切换开关状态"));
        inventory.setItem(num++,buildMenuItem(Material.JUNGLE_SIGN,"添加私人传送点","点击将当前位置标记为传送点"));
        return inventory;
    }
    private static ItemStack buildMenuItem(Material material, String name, String... lores){
        ItemStack itemStack = new ItemStack(material,1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "" +ChatColor.GOLD + name);
        List<String> lore = new ArrayList<String>();
        for (String str : lores){
            lore.add(ChatColor.RESET + str);
        }
        lore.add("点击传送至此地点");
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack buildMenuItem(Player player){
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD,1);
        SkullMeta itemMeta = (SkullMeta)itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "" +ChatColor.GOLD + player.getDisplayName());
        itemMeta.setOwningPlayer(player);
        List<String> lore = new ArrayList<String>();
        lore.add("点击传送至此玩家");
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
