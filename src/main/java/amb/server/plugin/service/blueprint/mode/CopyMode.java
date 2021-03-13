package amb.server.plugin.service.blueprint.mode;

import amb.server.plugin.config.PluginConfig;
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
import org.bukkit.plugin.PluginLogger;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static amb.server.plugin.service.utils.PlayerUtils.PLAYER_BLUEPRINT_SELECT;

/**
 * @author zhangrenjing
 * created on 2021/3/12
 */
public class CopyMode {
    private static final Logger LOGGER = PluginLogger.getLogger("Ambition");
    /**
     * 右键
     * @param player
     * @param location
     */
    public static void doUseEvent(Player player, Location location) {
        if (PlayerUtils.hasMark(player, PlayerUtils.PLAYER_BLUEPRINT_SELECT)) return;
        Location location2 = BlueprintUtil.setSelectedLocation2(player, location);
        Location location1 = BlueprintUtil.getSelectedLocation1(player);
        if (location1 != null) {
            if (location1.equals(location2)) {
                BlueprintUtil.delSelected1(player);
            } else if (BlueprintUtil.isValidRange(player, location1, location2)) {
                // 绘制粒子
                ParticleUtils.drawLineTimer(location1, location2);
                BlueprintUtil.delSelected3(player);
                player.sendMessage("[建筑蓝图] Copy 区域选择完成");
                GUIUtils.sendMsg(player, "请通过[潜行+使用]/[Shift+右键]标记复制点");
            }
        }
    }

    /**
     * 左键
     * @param player
     * @param location
     */
    public static void doTouchEvent(Player player, Location location) {
        if (PlayerUtils.hasMark(player, PlayerUtils.PLAYER_BLUEPRINT_SELECT)) return;
        Location location1 = BlueprintUtil.setSelectedLocation1(player, location);
        Location location2 = BlueprintUtil.getSelectedLocation2(player);
        if (location2 != null) {
            if (location1.equals(location2)) {
                BlueprintUtil.delSelected2(player);
            } else if (BlueprintUtil.isValidRange(player, location1, location2)) {
                // 绘制粒子
                ParticleUtils.drawLineTimer(location1, location2);
                BlueprintUtil.delSelected3(player);
                player.sendMessage("[建筑蓝图] Copy 区域选择完成");
                GUIUtils.sendMsg(player, "请通过[潜行+使用]/[Shift+右键]标记复制点");
            }
        }
    }

    /**
     * 潜行 + 右键
     * @param player
     */
    public static void doShiftUseEvent(Player player) {
        Location location1 = BlueprintUtil.getSelectedLocation1(player);
        Location location2 = BlueprintUtil.getSelectedLocation2(player);
        if (location1 == null || location2 == null) {
            player.sendMessage("[建筑蓝图] 请先选择需要复制的区域");
            return;
        }
        Location location3 = BlueprintUtil.getSelectedLocation3(player);
        if (location3 == null) {
            BlueprintUtil.setSelectedLocation3(player, player.getEyeLocation());
            PlayerUtils.mark(player, PlayerUtils.PLAYER_BLUEPRINT_SELECT);
            GUIUtils.sendMsg(player, "再次[潜行+使用]/[Shift+右键]可以进行粘贴");
        } else {
            // 代扣粘贴材料的填充菜单
            Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.blueprintCopyItemPutName);
            player.openInventory(inventory);
        }

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
        Location pos3 = BlueprintUtil.getSelectedLocation3(player);
        if (pos1 == null || pos2 == null || pos3 == null || pos1.getWorld() == null || pos1.getWorld() != pos2.getWorld()
                || pos1.getWorld() != pos3.getWorld() || pos1.getWorld() != player.getWorld()) {
            player.sendMessage("[建筑蓝图] 选择位置无效！");
            return;
        }
        // 删除选择的区域并开始执行蓝图操作
        BlueprintUtil.delSelected(player);
        int[] xyzRange = BlueprintUtil.getRange(pos1, pos2);
        World world = pos1.getWorld();

        // 异步运算
        asyncCalculation(player, validItem, world, xyzRange, pos3.clone());
    }

    private static void asyncCalculation(Player player, List<ItemStack> validItem, World world, int[] xyzRange, Location copyPos) {
        // 不能异步操作块
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PluginCore.getInstance(), () -> {
            Location playerPos = player.getEyeLocation().clone();
            Map<Material, List<ItemStack>> materialListMap = validItem.stream().collect(Collectors.groupingBy(i -> i.getType()));
            Map<Block, Material> needProcessBlockMap = new HashMap<>();
            // 计算旋转角度
            double[][] rotateMatrix = customAngle(copyPos, playerPos);
            copyPos.add(-0.5, 0, -0.5);
            for (int x = xyzRange[0]; x <= xyzRange[1]; x++) {
                for (int y = xyzRange[2]; y <= xyzRange[3]; y++) {
                    for (int z = xyzRange[4]; z <= xyzRange[5]; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (BlueprintUtil.isValueCopyBlock(block) && materialListMap.containsKey(block.getType())) {
                            // 计算位置偏移量
                            Location subtract = customMove(copyPos, playerPos, block, rotateMatrix);
                            Block target = world.getBlockAt(subtract);
                            if (target.isEmpty() || target.isPassable()) {
                                Iterator<ItemStack> itemStackIterator = materialListMap.get(block.getType()).iterator();
                                ItemStack index = itemStackIterator.next();
                                while (index.getAmount() <= 0 && itemStackIterator.hasNext()) {
                                    index = itemStackIterator.next();
                                }
                                if (index.getAmount() > 0) {
                                    //异步处理构建效果
                                    needProcessBlockMap.put(target, index.getType());
                                    index.setAmount(index.getAmount() - 1);
                                } else {
                                    materialListMap.remove(block.getType());
                                }
                            }
                        }
                        if (materialListMap.isEmpty()) {
                            BlueprintUtil.syncBuild(needProcessBlockMap);
                            player.sendMessage("[建筑蓝图] 材料不足！无法全部复制");
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

    private static double[][] customAngle(Location copyPos, Location playerPos) {
        Vector copyV = customNormalize(copyPos.getDirection().clone());
        Vector playerV = customNormalize(playerPos.getDirection().clone());
        if (copyV.getX() == playerV.getX() && copyV.getZ() == playerV.getZ()) {
            // 0 度
            return new double[][]{{1, 0}, {0, 1}};
        } else if ((copyV.getX() + playerV.getX()) == 0 && (copyV.getZ() + playerV.getZ()) == 0) {
            // 180 度
            return new double[][]{{-1, 0}, {0, -1}};
        } else if ((copyV.getX() * playerV.getZ() - copyV.getZ() * playerV.getX()) > 0){
            // +90 度
            return new double[][]{{0, -1}, {1, 0}};
        } else {
            // -90 度
            return new double[][]{{0, 1}, {-1, 0}};
        }
    }

    private static Vector customNormalize(Vector vector) {
        vector.setY(0);
        double absX = Math.abs(vector.getX());
        double absZ = Math.abs(vector.getZ());
        if (absX > absZ) {
            vector.setZ(0);
            vector.setX(vector.getX()/absX);
        } else {
            vector.setX(0);
            vector.setZ(vector.getZ()/absZ);
        }
        return vector;
    }

    private static Location customMove(Location copyPos, Location playerPos, Block block, double[][] rotateMatrix) {
        Location subtract = block.getLocation().clone().subtract(copyPos);
        double originX = subtract.getX();
        double originZ = subtract.getZ();
        subtract.setX(originX * rotateMatrix[0][0] + originZ * rotateMatrix[0][1]);
        subtract.setZ(originX * rotateMatrix[1][0] + originZ * rotateMatrix[1][1]);
        return playerPos.clone().add(subtract);
    }
}
