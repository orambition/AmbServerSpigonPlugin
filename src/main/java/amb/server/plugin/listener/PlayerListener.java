package amb.server.plugin.listener;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.service.blueprint.BlueprintService;
import amb.server.plugin.service.blueprint.BlueprintUtil;
import amb.server.plugin.service.blueprint.mode.CopyMode;
import amb.server.plugin.service.blueprint.mode.CylinderMode;
import amb.server.plugin.service.blueprint.mode.FillingMode;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.radar.RadarService;
import amb.server.plugin.service.tpb.TpBookGUI;
import amb.server.plugin.service.tpb.TpBookItem;
import amb.server.plugin.service.tpb.TpBookService;
import amb.server.plugin.service.utils.PlayerUtils;
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
import static amb.server.plugin.service.utils.PlayerUtils.*;

public class PlayerListener implements Listener {
    private final static Logger LOGGER = PluginLogger.getLogger("Ambition");

    /**
     * ���ʹ����Ʒ
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
            /** ʹ�ô����� **/
            TpBookService.useTpBookEvent(event);
        } else if (item.getType().equals(PluginConfig.radarItem)
                && item.getItemMeta().getDisplayName().equals(PluginConfig.radarName)) {
            /** ʹ�������״� **/
            RadarService.useRadarEvent(event);
        } else if (item.getType().equals(PluginConfig.blueprintSelectorItem)) {
            /** ʹ�� ������ͼѡ���� **/
            BlueprintService.useSelectorEvent(event);
        }
    }

    @EventHandler
    public void onPlayerClickMenu(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(PluginConfig.tpBookMenuTitle)
                && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST
                && event.getCurrentItem() != null) {
            event.setCancelled(true);
            // ������������
            TpBookService.clickViewMenuEvent(event);
        } else if (event.getView().getTitle().equals(PluginConfig.radarName)
                && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
            // ����״����
            if (event.getSlot() != 11 && event.getSlot() != 15) {
                event.setCancelled(true);
            }
        } else if (event.getView().getTitle().equals(BlueprintService.BLUEPRINT_MODE_SELECT_MENU)
                && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
            // ������ͼ ģʽѡ�����
            event.setCancelled(true);
            BlueprintService.doClickMenuEvent(event);
        } else if (event.getView().getTitle().equals(FillingMode.BLUEPRINT_FILLING_PUT_MENU)
                || event.getView().getTitle().equals(CopyMode.BLUEPRINT_COPY_PUT_MENU)) {
            // ������ͼ �������ҳ�棬���Ʒ���Ĳ���
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
            // �ر��״����ý���
            RadarService.setTargetAndPower((Player) event.getPlayer(), event.getInventory().getItem(11), event.getInventory().getItem(15));
        } else if (event.getView().getTitle().equals(FillingMode.BLUEPRINT_FILLING_PUT_MENU)) {
            // �رս�����ͼ ����������
            FillingMode.closeMenu((Player) event.getPlayer(), event.getInventory().getContents());
        } else if (event.getView().getTitle().equals(CopyMode.BLUEPRINT_COPY_PUT_MENU)) {
            CopyMode.closeMenu((Player) event.getPlayer(), event.getInventory().getContents());
        } else if (event.getView().getTitle().equals(CylinderMode.BLUEPRINT_CYLINDER_PUT_MENU)) {
            CylinderMode.closeMenu((Player) event.getPlayer(), event.getInventory().getContents());
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
                player.sendMessage(ChatColor.RED + "������������");
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
            // �����ǰ�д�����
            player.removeScoreboardTag("deadTp-haveBook");
            player.getInventory().setItemInMainHand(TpBookItem.getItem(tpBookTpPrice));
        }
    }
}
