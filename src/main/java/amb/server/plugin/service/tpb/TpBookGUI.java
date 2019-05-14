package amb.server.plugin.service.tpb;

import amb.server.plugin.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class TpBookGUI {

    public static void openBook(Player player){
        player.sendMessage("openbook");
        player.openInventory(getInventoryMenu(player.getDisplayName()));
    }

    private static Inventory getInventoryMenu(String playerName){
        String title = PluginConfig.pluginConfig.getString("tpb.menu.title").replace("%s",playerName);
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack itemStack = new ItemStack(Material.RED_BANNER,1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("公共地点1");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("地点介绍");
        lore.add("地点介绍");
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        inventory.addItem(itemStack);
        return inventory;
    }


}
