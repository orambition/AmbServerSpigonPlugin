package amb.server.plugin.listener;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.service.blueprint.BlueprintService;
import amb.server.plugin.service.blueprint.BlueprintUtil;
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
                && event.getCurrentItem() != null) {
            // ������������
            TpBookService.clickViewMenuEvent(event);
        } else if (event.getView().getTitle().equals(PluginConfig.radarName)
                && event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.CHEST) {
            // ����״����
            if (event.getSlot() != 11 && event.getSlot() != 15) {
                event.setCancelled(true);
            }
        } else if (event.getView().getTitle().equals(PluginConfig.blueprintBuildItemPutName)) {
            // ������ͼ �������ҳ�棬���Ʒ���Ĳ���
            ItemStack currentItem = event.getCursor() != null && event.getCursor().getType() != Material.AIR ?
                    event.getCursor() : event.getCurrentItem();
            if (!BlueprintUtil.isValueBuildItem(currentItem)) {
                event.setCancelled(true);
            }
        } else if (event.getView().getTitle().equals(PluginConfig.blueprintBreakItemPutName)) {
            // ������ͼ �������ҳ�棬���Ʒ���Ĺ���
            ItemStack currentItem = event.getCursor() != null && event.getCursor().getType() != Material.AIR ?
                    event.getCursor() : event.getCurrentItem();
            if (!BlueprintUtil.isValueBreakItem(currentItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(PluginConfig.radarName)) {
            // �ر��״����ý���
            RadarService.setTargetAndPower((Player) event.getPlayer(), event.getInventory().getItem(11), event.getInventory().getItem(15));
        } else if (event.getView().getTitle().equals(PluginConfig.blueprintBuildItemPutName)) {
            // �رս�����ͼ ����������
            BlueprintService.doBlueprint((Player) event.getPlayer(), event.getInventory().getContents(), false);
        } else if (event.getView().getTitle().equals(PluginConfig.blueprintBreakItemPutName)) {
            // �رս�����ͼ ����ѡ�����
            BlueprintService.doBlueprint((Player) event.getPlayer(), event.getInventory().getContents(), true);
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
