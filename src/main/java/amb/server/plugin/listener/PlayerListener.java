package amb.server.plugin.listener;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.radar.RadarService;
import amb.server.plugin.service.tpb.TpBookGUI;
import amb.server.plugin.service.tpb.TpBookItem;
import amb.server.plugin.service.tpb.TpBookService;
import org.bukkit.ChatColor;
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
     * @param event
     */
    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }
        ItemStack item = event.getItem();
        //logger.info("2:"+item.getType()+"=="+PluginConfig.tpBookItem +"&&"+ item.getItemMeta().getDisplayName() +"=="+ PluginConfig.tpBookTitle);
        if (item.getType().equals(PluginConfig.tpBookItem) && item.getItemMeta().getDisplayName().equals(PluginConfig.tpBookTitle)) {
            /** ʹ�ô����� **/
            Player player = event.getPlayer();
            if (!player.hasPermission(PermissionConstant.TPB)){
                player.sendMessage("��Ȩ��ʹ��!����ϵAmb");
                return;
            }
            if (player.isSneaking()) {
                // ǰ��(Shift+)ʱ�����п��ٴ���
                event.setCancelled(true);
                TpBookService.doShiftClickAction(player);
            } else {
                Action action = event.getAction();
                if ((action.equals(Action.RIGHT_CLICK_BLOCK) && !event.getClickedBlock().getType().isInteractable()) || action.equals(Action.RIGHT_CLICK_AIR)){
                    event.setCancelled(true);
                    TpBookGUI.openBook(event.getPlayer());
                } else if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)){
                    event.setCancelled(true);
                    TpBookGUI.openMenu(event.getPlayer());
                }
            }
        } else if (item.getType().equals(PluginConfig.radarItem) && item.getItemMeta().getDisplayName().equals(PluginConfig.radarName)){
            /** ʹ�������״� **/
            Player player = event.getPlayer();
            if (!player.hasPermission(PermissionConstant.RADER)){
                player.sendMessage("��Ȩ��ʹ��!����ϵAmb");
                return;
            }
            Action action = event.getAction();
            if ((action.equals(Action.RIGHT_CLICK_BLOCK) && !event.getClickedBlock().getType().isInteractable()) || action.equals(Action.RIGHT_CLICK_AIR)){
                // �Ҽ�
                event.setCancelled(true);
                RadarService.user(player, event.getItem());
            } else if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)){
                // ���
                event.setCancelled(true);
                RadarService.open(player, event.getItem());
            }
        }
    }

    @EventHandler
    public void onPlayerClickMenu(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(PluginConfig.tpBookMenuTitle)
                && event.getCurrentItem() != null) {
            // ������������
            event.setCancelled(true);
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
                Player player = (Player) event.getWhoClicked();
                if (!player.hasPermission(PermissionConstant.TPB)){
                    player.sendMessage("��Ȩ��ʹ��!����ϵAmb");
                    return;
                }
                ItemStack clickedItem = event.getCurrentItem();
                player.updateInventory();
                player.closeInventory();
                boolean delete = event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && event.getClick().isRightClick();
                boolean setFast = event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && event.getClick().isLeftClick();
                TpBookService.doClickAction(player, clickedItem, delete, setFast);
            }
        }
        if (event.getView().getTitle().equals(PluginConfig.radarName)
                && event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.CHEST){
            // ����״����
            if (event.getSlot() != 11 && event.getSlot() != 15){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if (event.getView().getTitle().equals(PluginConfig.radarName)){
            // �ر��״����ý���
            RadarService.setTargetAndPower((Player) event.getPlayer(), event.getInventory().getItem(11), event.getInventory().getItem(15));
        }
    }

    @EventHandler
    public void onPlayerDead(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.hasPermission(PermissionConstant.TPB)){
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
        if (!player.hasPermission(PermissionConstant.TPB)){
            return;
        }
        if (player.getScoreboardTags().contains("deadTp-haveBook")) {
            // �����ǰ�д�����
            player.removeScoreboardTag("deadTp-haveBook");
            player.getInventory().setItemInMainHand(TpBookItem.getItem(tpBookTpPrice));
        }
    }
}
