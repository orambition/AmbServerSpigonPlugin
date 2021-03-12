package amb.server.plugin.listener;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.service.blueprint.BlueprintService;
import amb.server.plugin.service.blueprint.BlueprintUtil;
import amb.server.plugin.service.blueprint.mode.CopyMode;
import amb.server.plugin.service.blueprint.mode.FillingMode;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.radar.RadarService;
import amb.server.plugin.service.tpb.TpBookGUI;
import amb.server.plugin.service.tpb.TpBookItem;
import amb.server.plugin.service.tpb.TpBookService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginLogger;

import java.util.logging.Logger;

import static amb.server.plugin.config.PluginConfig.tpBookTpPrice;
import static amb.server.plugin.service.tpb.TpBookDataService.addPlayerDeadTeleporter;

public class PlayerListener implements Listener {
    private final Logger logger = PluginLogger.getLogger("Ambition");

    /**
     * 玩家使用物品
     *
     * @param event
     */
    @EventHandler
    public static void onPlayerUseItem(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }
        ItemStack item = event.getItem();

        if (item.getType().equals(PluginConfig.tpBookItem)
                && item.getItemMeta().getDisplayName().equals(PluginConfig.tpBookTitle)) {
            /** 使用传送书 **/
            TpBookService.useTpBookEvent(event);
        } else if (item.getType().equals(PluginConfig.radarItem)
                && item.getItemMeta().getDisplayName().equals(PluginConfig.radarName)) {
            /** 使用万能雷达 **/
            RadarService.useRadarEvent(event);
        } else if (item.getType().equals(PluginConfig.blueprintSelectorItem)) {
            /** 使用 建筑蓝图选择器 **/
            BlueprintService.useSelectorEvent(event);
        }
    }

    @EventHandler
    public void onPlayerClickMenu(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(PluginConfig.tpBookMenuTitle)
                && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST
                && event.getCurrentItem() != null) {
            event.setCancelled(true);
            // 点击传送书界面
            TpBookService.clickViewMenuEvent(event);
        } else if (event.getView().getTitle().equals(PluginConfig.radarName)
                && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
            // 点击雷达界面
            if (event.getSlot() != 11 && event.getSlot() != 15) {
                event.setCancelled(true);
            }
        } else if (event.getView().getTitle().equals(PluginConfig.blueprintModeSelectMenu)
                && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
            // 建筑蓝图 模式选择界面
            event.setCancelled(true);
            BlueprintService.doClickMenuEvent(event);
        } else if (event.getView().getTitle().equals(PluginConfig.blueprintBuildItemPutName)
                || event.getView().getTitle().equals(PluginConfig.blueprintCopyItemPutName)) {
            // 建筑蓝图 材料填充页面，限制放入的材料
            ItemStack currentItem = event.getCursor() != null && event.getCursor().getType() != Material.AIR ?
                    event.getCursor() : event.getCurrentItem();
            if (!BlueprintUtil.isValueBuildItem(currentItem)) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(PluginConfig.radarName)) {
            // 关闭雷达设置界面
            RadarService.setTargetAndPower((Player) event.getPlayer(), event.getInventory().getItem(11), event.getInventory().getItem(15));
        } else if (event.getView().getTitle().equals(PluginConfig.blueprintBuildItemPutName)) {
            // 关闭建筑蓝图 材料填充界面
            FillingMode.closeMenu((Player) event.getPlayer(), event.getInventory().getContents());
        } else if (event.getView().getTitle().equals(PluginConfig.blueprintCopyItemPutName)) {
            CopyMode.closeMenu((Player) event.getPlayer(), event.getInventory().getContents());
        }
    }

    @EventHandler
    public void onPlayerDead(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.hasPermission(PermissionConstant.TPB)) {
            return;
        }
        for (ItemStack item : event.getDrops()) {
            if (item.getType().equals(PluginConfig.tpBookItem) && item.getItemMeta().getDisplayName().equals(PluginConfig.tpBookTitle)) {
                event.getDrops().remove(item);
                player.addScoreboardTag("deadTp-haveBook");
                addPlayerDeadTeleporter(event.getEntity());
                player.sendMessage(ChatColor.RED + "传送书已消耗");
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PermissionConstant.TPB)) {
            return;
        }
        if (player.getScoreboardTags().contains("deadTp-haveBook")) {
            // 玩家死前有传送书
            player.removeScoreboardTag("deadTp-haveBook");
            player.getInventory().setItemInMainHand(TpBookItem.getItem(tpBookTpPrice));
        }
    }
}
