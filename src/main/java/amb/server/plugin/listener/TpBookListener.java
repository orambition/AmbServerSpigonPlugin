package amb.server.plugin.listener;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.service.tpb.TpBookGUI;
import amb.server.plugin.service.tpb.TpBookService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TpBookListener implements Listener {

    @EventHandler
    public void onPlayerOpenBook(PlayerInteractEvent event){
        if (event.hasItem() && event.getItem().getType().equals(Material.WRITTEN_BOOK)){
            if (event.getItem().getItemMeta().getDisplayName().equals(PluginConfig.tpBookTitle)){
                event.setCancelled(true);
                TpBookGUI.openBook(event.getPlayer());
            }
        }

    }

    @EventHandler
    public void onPlayerClickMenu(InventoryClickEvent event){
        if (event.getView().getTitle().equals(PluginConfig.tpBookMenuTitle)
                && event.getCurrentItem() != null){
            event.setCancelled(true);
            if (event.getClickedInventory().getType() == InventoryType.CHEST){
                Player player = (Player) event.getWhoClicked();
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem.getType() == Material.JUNGLE_SIGN){
                    TpBookService.addPrivateTeleporter(player, "private");
                }
                //TpBookService.addPublicTeleporter(player, "haha");
                player.closeInventory();
            }
        }
    }
}
