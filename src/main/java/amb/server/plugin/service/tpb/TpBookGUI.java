package amb.server.plugin.service.tpb;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.model.Telepoter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TpBookGUI {

    public static void openBook(Player player){
        player.sendMessage("openbook");
        player.openInventory(getInventoryMenu(player.getDisplayName()));
    }

    private static Inventory getInventoryMenu(String playerName){

        Inventory inventory = Bukkit.createInventory(null, 27, PluginConfig.tpBookMenuTitle);
        ItemStack itemStack = new ItemStack(Material.RED_BANNER,1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("公共地点1");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("地点介绍");
        lore.add("地点介绍");
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        inventory.addItem(itemStack);
        for (Telepoter telepoter : TpBookService.getAllPublicTeleporter()){
            inventory.addItem(buildMenuItem(telepoter));
        }

        return inventory;
    }
    private static ItemStack buildMenuItem(Telepoter telepoter){
        ItemStack itemStack = new ItemStack(Material.RED_BANNER,1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(telepoter.getName());
        List<String> lore = new ArrayList<String>();
        lore.add("创建者:"+telepoter.getAuthor());
        lore.add("点击传送至此地点");
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }


}
