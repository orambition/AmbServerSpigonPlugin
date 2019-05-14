package amb.server.plugin.listener;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.service.tpb.TpBookGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class TpBookListener implements Listener {

    @EventHandler
    public void onPlayerOpenBook(PlayerInteractEvent event){
        if (event.hasItem() && event.getItem().getType().equals(Material.WRITTEN_BOOK)){
            event.getPlayer().sendMessage(event.getItem().toString());
            event.getPlayer().sendMessage(PluginConfig.pluginConfig.getString("tpb.book.title"));
            if (event.getItem().getItemMeta().getDisplayName().equals(PluginConfig.pluginConfig.getString("tpb.book.title"))){
                event.setCancelled(true);
                TpBookGUI.openBook(event.getPlayer());
            }
        }

    }

    @EventHandler
    public void onPlayerClickMenu(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        String title = PluginConfig.pluginConfig.getString("tpb.menu.title").replace("%s",player.getDisplayName());
        if (event.getView().getTitle().equals(title)){
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            player.sendMessage("CLICK"+clickedItem.toString());
        }
    }
}
