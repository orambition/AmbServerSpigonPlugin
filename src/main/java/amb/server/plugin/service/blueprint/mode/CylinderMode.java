package amb.server.plugin.service.blueprint.mode;

import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.blueprint.BlueprintUtil;
import amb.server.plugin.service.utils.GUIUtils;
import amb.server.plugin.service.utils.ParticleUtils;
import amb.server.plugin.service.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

import static amb.server.plugin.service.utils.PlayerUtils.PLAYER_BLUEPRINT_SELECT;

/**
 * @author zhangrenjing
 * created on 2021/3/12
 * 圆柱 模式
 */
public class CylinderMode {
    public static final String BLUEPRINT_CYLINDER_PUT_MENU = "建筑蓝图 - 请放入圆柱的建造材料[材料]";

    public static void doUseEvent(Player player, Location location) {
        Location location2 = BlueprintUtil.setSelectedLocation2(player, location);
        Location location1 = BlueprintUtil.getSelectedLocation1(player);
        if (location1 != null) {
            if (location1.equals(location2)) {
                BlueprintUtil.delSelected1(player);
            } else if (BlueprintUtil.isValidRange(player, location1, location2)) {
                openMenu(player, location1, location2);
            }
        }
    }

    public static void doTouchEvent(Player player, Location location) {
        Location location1 = BlueprintUtil.setSelectedLocation1(player, location);
        Location location2 = BlueprintUtil.getSelectedLocation2(player);
        if (location2 != null) {
            if (location1.equals(location2)) {
                BlueprintUtil.delSelected2(player);
            } else if (BlueprintUtil.isValidRange(player, location1, location2)) {
                openMenu(player, location1, location2);
            }
        }
    }

    /**
     * 填充预处理，仅做校验，打开材料/工具填充界面，
     * 监听页面关闭事件，开始填充/破坏
     *
     * @param player
     * @param pos1
     * @param pos2
     */
    public static void openMenu(Player player, Location pos1, Location pos2) {
        // 加串行锁，同时只能执行一个任务
        PlayerUtils.mark(player, PLAYER_BLUEPRINT_SELECT);
        // 绘制粒子
        ParticleUtils.drawLineTimer(pos1, pos2);

        // 建造, 这个方法是当前线程延时执行，不是真正的"异步"的，背包无法在异步线程中打开，延时打开是为了让玩家读完提示
        Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
            Inventory inventory = Bukkit.createInventory(null, 54, BLUEPRINT_CYLINDER_PUT_MENU);
            player.openInventory(inventory);
        }, 30);
        GUIUtils.sendMsg(player, "区域选择成功，请放入建造材料");
        //player.sendMessage("[建筑蓝图] 区域选择成功，请放入建造[材料]");
    }

    /**
     * 玩家关闭 材料填充 界面
     *
     * @param player
     * @param itemContents
     */
    public static void closeMenu(Player player, ItemStack[] itemContents) {
        PlayerUtils.unMark(player, PLAYER_BLUEPRINT_SELECT);
        if (itemContents == null || itemContents.length == 0) {
            return;
        }
        // 过滤出有效的物品
        List<ItemStack> validItem = new ArrayList<>();
        for (ItemStack itemStack : itemContents) {
            if (itemStack == null) continue;
            if (BlueprintUtil.isValueBuildItem(itemStack)) {
                validItem.add(itemStack);
            } else {
                player.getWorld().dropItem(player.getLocation(), itemStack);
            }
        }
        if (validItem.isEmpty()) return;
        // 校验位置是否还有效
        Location pos1 = BlueprintUtil.getSelectedLocation1(player);
        Location pos2 = BlueprintUtil.getSelectedLocation2(player);
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || pos1.getWorld() != pos2.getWorld()) {
            player.sendMessage("[建筑蓝图] 选择位置无效！");
            return;
        }
        // 删除选择的区域并开始执行蓝图操作
        BlueprintUtil.delSelected(player);
        // 填充
        asyncBuild(player, validItem, pos1, pos2);
    }

    /**
     * 异步建造
     *
     * @param player
     * @param validItem
     * @param pos1      圆心
     * @param pos2      半径 + 高度
     */
    private static void asyncBuild(Player player, List<ItemStack> validItem, Location pos1, Location pos2) {
        // 不能异步操作块
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PluginCore.getInstance(), () -> {
            Map<Block, Material> needProcessBlockMap = new HashMap<>();
            Iterator<ItemStack> itemStackIterator = validItem.iterator();
            ItemStack index = itemStackIterator.next();
            List<Location> roundXZ = getCylinderBlockLocation(pos1, pos2);
            // 高度
            int high = (int) (pos2.getY() - pos1.getY());
            int positiveNegative = high >= 0 ? 1 : -1;
            high += positiveNegative;
            for (int i = 0; i != high; i += positiveNegative) {
                for (Location location : roundXZ) {
                    Block block = location.clone().add(0, i, 0).getBlock();
                    if (block.isEmpty() || block.isPassable()) {
                        while (index.getAmount() <= 0 && itemStackIterator.hasNext()) {
                            index = itemStackIterator.next();
                        }
                        if (index.getAmount() > 0) {
                            needProcessBlockMap.put(block, index.getType());
                            index.setAmount(index.getAmount() - 1);
                        } else {
                            player.sendMessage("[建筑蓝图] 材料不足！无法完成建造");
                            BlueprintUtil.syncBuild(needProcessBlockMap);
                            return;
                        }
                    }
                }
            }

            // 填充
            BlueprintUtil.syncBuild(needProcessBlockMap);
            // 归还材料
            List<ItemStack> backList = validItem.stream().filter(i -> i.getAmount() > 0).collect(Collectors.toList());
            if (!backList.isEmpty()) {
                BlueprintUtil.syncBackBuildItem(player, backList);
            }
        });
    }

    private static List<Location> getCylinderBlockLocation(Location pos1, Location pos2) {
        // 半径 的 平方
        double radiusPow = Math.pow(pos2.getX() - pos1.getX(), 2) + Math.pow(pos2.getZ() - pos1.getZ(), 2);
        // 半径
        int radius = (int) Math.round(Math.sqrt(radiusPow));
        // 计算圆周方块
        List<Location> roundXZ = new ArrayList<>();
        Location temp = pos1.clone().add(0.5, 0, 0.5);

        int middle = (int) Math.round(Math.sqrt(radiusPow / 2));
        for (int i = 0; i <= middle; i++) {
            int rZ = (int) Math.round(Math.sqrt(radiusPow - Math.pow(i, 2)));
            roundXZ.add(temp.clone().add(i, 0, rZ));
            roundXZ.add(temp.clone().add(i, 0, -rZ));
            roundXZ.add(temp.clone().add(rZ, 0, i));
            roundXZ.add(temp.clone().add(-rZ, 0, i));
            if (i == 0) continue;
            roundXZ.add(temp.clone().add(-i, 0, rZ));
            roundXZ.add(temp.clone().add(-i, 0, -rZ));
            roundXZ.add(temp.clone().add(rZ, 0, -i));
            roundXZ.add(temp.clone().add(-rZ, 0, -i));
        }
        return roundXZ;
    }

}
