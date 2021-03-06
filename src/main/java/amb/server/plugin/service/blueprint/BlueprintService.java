package amb.server.plugin.service.blueprint;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.tpb.TpBookGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangrenjing
 * created on 2021/3/6
 */
public class BlueprintService {
    public static final String SELECT_LOCATION_1 = "selected pos 1";
    public static final String SELECT_LOCATION_2 = "selected pos 2";

    /**
     * 使用 蓝图选择器
     *
     * @param event
     */
    public static void useSelectorEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PermissionConstant.BLUEPRINT)) {
            player.sendMessage("[建筑蓝图]无权限使用!请联系Amb");
            return;
        }
        // event.setCancelled(true);
        // 前行(Shift+)
        if (player.isSneaking()) {

        } else {
            Action action = event.getAction();
            // (右击快 && 物体不是可交互的) || 右键空气
            if (action.equals(Action.LEFT_CLICK_BLOCK)) {
                Location location1 = BlueprintUtil.setSelectedLocation(player, SELECT_LOCATION_1, event.getClickedBlock().getLocation());
                Location location2 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_2);
                if (location2 != null) {
                    preprocessed(player, location1, location2);
                }
            } else if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                Location location2 = BlueprintUtil.setSelectedLocation(player, SELECT_LOCATION_2, event.getClickedBlock().getLocation());
                Location location1 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_1);
                if (location1 != null) {
                    preprocessed(player, location1, location2);
                }
            }
        }
    }

    /**
     * 填充预处理，仅做校验，打开材料填充界面，
     * 监听页面关闭事件，开始填充
     *
     * @param player
     * @param pos1
     * @param pos2
     */
    private static void preprocessed(Player player, Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || !Objects.equals(pos1.getWorld(), pos2.getWorld())) {
            return;
        }

        int xSize = Math.abs(pos1.getBlockX() - pos2.getBlockX());
        int ySize = Math.abs(pos1.getBlockY() - pos2.getBlockY());
        int zSize = Math.abs(pos1.getBlockZ() - pos2.getBlockZ());
        if (xSize > PluginConfig.blueprintSelectorMaxRange
                || ySize > PluginConfig.blueprintSelectorMaxRange
                || zSize > PluginConfig.blueprintSelectorMaxRange) {
            player.sendMessage("[建筑蓝图]选择范围过大，请重新选择！");
            return;
        }
        Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.blueprintBuildItemPutName);
        player.openInventory(inventory);
    }

    public static void doPut(Player player, ItemStack[] materialsContents) {
        if (materialsContents == null || materialsContents.length == 0) {
            return;
        }
        List<ItemStack> validItem = new ArrayList<>();
        for (ItemStack itemStack : materialsContents) {
            if (itemStack == null) continue;
            if (itemStack.getType().isBlock() && itemStack.getType().isSolid() && itemStack.getAmount() > 0) {
                validItem.add(itemStack);
            } else {
                player.getWorld().dropItem(player.getLocation(), itemStack);
            }
        }
        if (validItem.size() == 0) {
            return;
        }

        Location pos1 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_1);
        Location pos2 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_2);
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || pos1.getWorld() != pos2.getWorld()) {
            player.sendMessage("[建筑蓝图]选择位置无效！");
            return;
        }
        BlueprintUtil.delSelected(player);

        int p1x = pos1.getBlockX();
        int p1y = pos1.getBlockY();
        int p1z = pos1.getBlockZ();

        int p2x = pos2.getBlockX();
        int p2y = pos2.getBlockY();
        int p2z = pos2.getBlockZ();

        int xMin, yMin, zMin, xMax, yMax, zMax;
        if (p1x < p2x) {
            xMin = p1x;
            xMax = p2x;
        } else {
            xMin = p2x;
            xMax = p1x;
        }
        if (p1y < p2y) {
            yMin = p1y;
            yMax = p2y;
        } else {
            yMin = p2y;
            yMax = p1y;
        }
        if (p1z < p2z) {
            zMin = p1z;
            zMax = p2z;
        } else {
            zMin = p2z;
            zMax = p1z;
        }

        World world = pos1.getWorld();
        Iterator<ItemStack> itemStackIterator = validItem.iterator();
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            ItemStack index = itemStackIterator.next();
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.isEmpty() || block.isPassable()) {
                            while (index.getAmount() <= 0 && itemStackIterator.hasNext()) {
                                index = itemStackIterator.next();
                            }
                            if (index.getAmount() > 0) {
                                block.setType(index.getType());
                                index.setAmount(index.getAmount() - 1);
                            } else {
                                player.sendMessage("[建筑蓝图]材料不足！建造提前结束");
                                return;
                            }
                        }
                    }
                }
            }
            validItem.stream().filter(i -> i.getAmount() > 0)
                    .forEach(i -> {
                        player.getWorld().dropItem(player.getLocation(), i);
                        player.sendMessage("[建筑蓝图]材料剩余：" + i.getItemMeta().getDisplayName() + "x" + i.getAmount());
                    });
        });
    }
}
