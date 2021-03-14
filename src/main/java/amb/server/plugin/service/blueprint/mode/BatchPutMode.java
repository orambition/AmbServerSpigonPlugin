package amb.server.plugin.service.blueprint.mode;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.blueprint.BlueprintUtil;
import amb.server.plugin.service.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
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
 * ��������
 */
public class BatchPutMode {
    private static final double LIMIT_NUM = 0.54120D;
    /**
     * ѡ��Ŀ��
     *
     * @param player
     * @param location
     */
    public static void doTouchEvent(Player player, Location location) {
        Location location1 = BlueprintUtil.getSelectedLocation1(player);
        if (location1 != null && location1.equals(location)) {
            return;
        }
        BlueprintUtil.setSelectedLocation1(player, location);
        // ��������
        ParticleUtils.drawLineTimer(location, location);
    }

    /**
     * ִ�� ��������
     *
     * @param player
     */
    public static void doUseEvent(Player player) {
        Location location1 = BlueprintUtil.getSelectedLocation1(player);
        if (location1 == null) {
            player.sendMessage("[������ͼ] �������ѡ�񷽿�");
            return;
        }
        Block block = location1.getBlock();
        if (!isValueBuildBlock(block)) {
            player.sendMessage("[������ͼ] ѡ�񷽿��޷���������");
            return;
        }
        if (!player.getInventory().contains(block.getType())) {
            player.sendMessage("[������ͼ] ���ϲ��㣡������û����ѡ��ķ���");
            return;
        }
        player.playSound(location1, Sound.BLOCK_NOTE_BLOCK_BASS, 2, 1);
        asyncCalculation(block, player.getEyeLocation().getDirection().normalize().clone(), Arrays.asList(player.getInventory().getContents()));
    }

    /**
     * ��Ч���������÷���
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
            // �����֦
            customNormalize(vector);
            List<ItemStack> usedItemList = inventoryContents.stream().filter(i -> i != null && i.getType() == block.getType()).collect(Collectors.toList());
            Location location = block.getLocation().clone().add(0.5, 0.5, 0.5).add(vector);

            List<Block> needProcessBlockList = new ArrayList<>();
            for (int i = 0; i < PluginConfig.blueprintBatchPutMaxCount; i++) {
                Block target = location.getBlock();
                if (target.isEmpty() || target.isPassable()) {
                    needProcessBlockList.add(target);
                } else {
                    break;
                }
                location = location.add(vector);
            }
            int needProcessBlockSize = needProcessBlockList.size();
            if (needProcessBlockSize == 0) {
                return;
            }
            ParticleUtils.drawLine(needProcessBlockList.get(0).getLocation(), needProcessBlockList.get(needProcessBlockSize-1).getLocation());
            syncBuild(needProcessBlockList, usedItemList, block.getBlockData().clone());
        });
    }

    private static Vector customNormalize(Vector vector) {
        double absX = Math.abs(vector.getX());
        // +0.366 �����·�Χ�ĽǶ� �� 45�� ������ 60��
        double absY = Math.abs(vector.getY()) + 0.366;
        double absZ = Math.abs(vector.getZ());
        if (absY >= absX && absY >= absZ) {
            vector.setX(0);
            vector.setZ(0);
        } else {
            vector.setY(0);
            if (Math.abs(absX - absZ) >= LIMIT_NUM) {
                // ����
                if (absX > absZ) {
                    vector.setZ(0);
                } else {
                    vector.setX(0);
                }
            } else {
                // 45�� ��
                vector.setX(vector.getX()/absX);
                vector.setZ(vector.getZ()/absZ);
            }
        }
        return vector.normalize();
    }
    private static void syncBuild(List<Block> needProcessBlockList, List<ItemStack> usedItemList, BlockData cloneBlockData) {
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            Iterator<ItemStack> itemStackIterator = usedItemList.iterator();
            ItemStack index = itemStackIterator.next();
            for (Block block : needProcessBlockList) {
                while (index.getAmount() <= 0 && itemStackIterator.hasNext()) {
                    index = itemStackIterator.next();
                }
                if (index.getAmount() > 0) {
                    //�첽������Ч�� block.setType(index.getType());
                    block.setType(index.getType());
                    block.setBlockData(cloneBlockData, true);
                    index.setAmount(index.getAmount() - 1);
                } else {
                    return;
                }
            }
        });
    }
}
