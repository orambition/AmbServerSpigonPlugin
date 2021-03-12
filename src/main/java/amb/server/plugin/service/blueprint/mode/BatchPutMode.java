package amb.server.plugin.service.blueprint.mode;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.blueprint.BlueprintUtil;
import amb.server.plugin.service.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhangrenjing
 * created on 2021/3/12
 */
public class BatchPutMode {
    /**
     * 选择目标
     *
     * @param player
     * @param location
     */
    public static void doTouchEvent(Player player, Location location) {
        BlueprintUtil.setSelectedLocation1(player, location);
        // 绘制粒子
        ParticleUtils.drawLineTimer(location, location);
    }

    /**
     * 执行 批量放置
     *
     * @param player
     * @param location
     */
    public static void doUseEvent(Player player, Location location) {
        Location location1 = BlueprintUtil.getSelectedLocation1(player);
        if (location1 == null) {
            player.sendMessage("[建筑蓝图] 请先左键选择方块");
            return;
        }
        Block block = player.getWorld().getBlockAt(location1);
        if (!isValueBuildBlock(block)) {
            player.sendMessage("[建筑蓝图] 选择方块无法批量放置");
            return;
        }
        if (!player.getInventory().contains(block.getType())) {
            player.sendMessage("[建筑蓝图] 材料不足！背包中没有所选择的方块");
            return;
        }
        asyncCalculation(block, player.getEyeLocation().getDirection().normalize(), Arrays.asList(player.getInventory().getContents()));
    }

    /**
     * 有效的批量放置方块
     *
     * @param block
     * @return
     */
    public static boolean isValueBuildBlock(Block block) {
        return block != null
                && block.getType().isBlock()
                && block.getType().isSolid()
                && !block.getType().name().contains("_DOOR");
    }

    private static void asyncCalculation(Block block, Vector vector, List<ItemStack> inventoryContents) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PluginCore.getInstance(), () -> {
            List<ItemStack> usedItemList = inventoryContents.stream().filter(i -> i != null && i.getType() == block.getType()).collect(Collectors.toList());
            Location location = block.getLocation().clone().add(0.5, 0.5, 0.5).add(vector);

            List<Block> needProcessBlockList = new ArrayList<>();
            for (int i = 0; i < PluginConfig.blueprintBatchPutMaxCount; i++) {
                Block target = block.getWorld().getBlockAt(location);
                if (target.isEmpty() || target.isPassable()) {
                    needProcessBlockList.add(target);
                } else {
                    break;
                }
                location = location.add(vector);
            }
            if (needProcessBlockList.isEmpty()) {
                return;
            }
            syncBuild(needProcessBlockList, usedItemList);
        });
    }

    private static void syncBuild(List<Block> needProcessBlockList, List<ItemStack> usedItemList) {
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            Iterator<ItemStack> itemStackIterator = usedItemList.iterator();
            ItemStack index = itemStackIterator.next();
            for (Block block : needProcessBlockList) {
                while (index.getAmount() <= 0 && itemStackIterator.hasNext()) {
                    index = itemStackIterator.next();
                }
                if (index.getAmount() > 0) {
                    //异步处理构建效果 block.setType(index.getType());
                    block.setType(index.getType());
                    index.setAmount(index.getAmount() - 1);
                } else {
                    return;
                }
            }
        });
    }
}
