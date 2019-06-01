package amb.server.plugin.listener;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.tpb.TpBookGUI;
import amb.server.plugin.service.tpb.TpBookItem;
import amb.server.plugin.service.tpb.TpBookService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import static amb.server.plugin.config.PluginConfig.tpBookTpPrice;
import static amb.server.plugin.service.tpb.TpBookDataService.addPlayerDeadTeleporter;

public class TpBookListener implements Listener {

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        player.sendTitle(ChatColor.GOLD + "��ӭ�ص�˼����", player.getDisplayName(), 10, 100, 20);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PluginCore.getInstance(), new Runnable() {
            public void run() {
                player.discoverRecipe(new NamespacedKey(PluginCore.getInstance(), "amb_plugin_tpbook"));
            }
        }, 500L);
    }

    @EventHandler
    public void onPlayerOpenBook(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }
        ItemStack item = event.getItem();
        if (item.getType().equals(PluginConfig.tpBookItem) && item.getItemMeta().getDisplayName().equals(PluginConfig.tpBookTitle)) {
            event.setCancelled(true);
            TpBookGUI.openBook(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerClickMenu(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(PluginConfig.tpBookMenuTitle)
                && event.getCurrentItem() != null) {
            event.setCancelled(true);
            if (event.getClickedInventory().getType() == InventoryType.CHEST) {
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
    public void onPlayerDead(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for(ItemStack item : event.getDrops()){
            if (item.getType().equals(PluginConfig.tpBookItem) && item.getItemMeta().getDisplayName().equals(PluginConfig.tpBookTitle)){
                event.getDrops().remove(item);
                player.addScoreboardTag("deadTp-haveBook");
                addPlayerDeadTeleporter(event.getEntity());
                player.sendMessage(ChatColor.RED + "������������");
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        if (player.getScoreboardTags().contains("deadTp-haveBook")){
            // �����ǰ�д�����
            player.removeScoreboardTag("deadTp-haveBook");
            player.getInventory().setItemInMainHand(TpBookItem.getItem(tpBookTpPrice));
        }
    }
}
