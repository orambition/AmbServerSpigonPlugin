package amb.server.plugin.listener;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.tpb.TpBookGUI;
import amb.server.plugin.service.tpb.TpBookService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;

import static amb.server.plugin.service.tpb.TpBookDataService.addPlayerDeadTeleporter;

public class TpBookListener implements Listener {

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event){
        final Player player = event.getPlayer();
        player.sendTitle(ChatColor.GOLD+"��ӭ�ص�˼����",player.getDisplayName(),10,100,20);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PluginCore.getInstance(), new Runnable() {
            public void run() {
                player.discoverRecipe(new NamespacedKey(PluginCore.getInstance(),"amb_plugin_tpbook"));
            }
        },100L);
    }

    @EventHandler
    public void onPlayerOpenBook(PlayerInteractEvent event){
        if (event.hasItem() && event.getItem().getType().equals(PluginConfig.tpBookItem)){
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
                player.updateInventory();
                player.closeInventory();
                boolean delete = event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && event.getClick().isRightClick();
                TpBookService.doClickAction(player, clickedItem, delete);
            }
        }
    }

    @EventHandler
    public void onPlayerDead(PlayerDeathEvent event){
        addPlayerDeadTeleporter(event.getEntity());
    }
}
