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
    public static final String BLUEPRINT_COPY_PUT_MENU = "������ͼ - �����ճ�������[����]";
    /**
     * �Ҽ�
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
                // ��������
                ParticleUtils.drawLineTimer(location1, location2);
                BlueprintUtil.delSelected3(player);
                player.sendMessage("[������ͼ] Copy ����ѡ�����");
                GUIUtils.sendMsg(player, "��ͨ��[Ǳ��+ʹ��]/[Shift+�Ҽ�]��Ǹ��Ƶ�");
            }
        }
    }

    /**
     * ���
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
                // ��������
                ParticleUtils.drawLineTimer(location1, location2);
                BlueprintUtil.delSelected3(player);
                player.sendMessage("[������ͼ] Copy ����ѡ�����");
                GUIUtils.sendMsg(player, "��ͨ��[Ǳ��+ʹ��]/[Shift+�Ҽ�]��Ǹ��Ƶ�");
            }
        }
    }

    /**
     * Ǳ�� + �Ҽ�
     * @param player
     */
    public static void doShiftUseEvent(Player player) {
        Location location1 = BlueprintUtil.getSelectedLocation1(player);
        Location location2 = BlueprintUtil.getSelectedLocation2(player);
        if (location1 == null || location2 == null) {
            player.sendMessage("[������ͼ] ����ѡ����Ҫ���Ƶ�����");
            return;
        }
        Location location3 = BlueprintUtil.getSelectedLocation3(player);
        if (location3 == null) {
            BlueprintUtil.setSelectedLocation3(player, player.getEyeLocation());
            PlayerUtils.mark(player, PlayerUtils.PLAYER_BLUEPRINT_SELECT);
            GUIUtils.sendMsg(player, "�ٴ�[Ǳ��+ʹ��]/[Shift+�Ҽ�]���Խ���ճ��");
        } else {
            // ����ճ�����ϵ����˵�
            Inventory inventory = Bukkit.createInventory(null, 54, BLUEPRINT_COPY_PUT_MENU);
            player.openInventory(inventory);
        }

    }

    /**
     * ��ҹر� ������� ����
     *
     * @param player
     * @param itemContents
     */
    public static void closeMenu(Player player, ItemStack[] itemContents) {
        PlayerUtils.unMark(player, PLAYER_BLUEPRINT_SELECT);
        if (itemContents == null || itemContents.length == 0) {
            return;
        }
        // ���˳���Ч����Ʒ
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
        // У��λ���Ƿ���Ч
        Location pos1 = BlueprintUtil.getSelectedLocation1(player);
        Location pos2 = BlueprintUtil.getSelectedLocation2(player);
        Location pos3 = BlueprintUtil.getSelectedLocation3(player);
        if (pos1 == null || pos2 == null || pos3 == null || pos1.getWorld() == null || pos1.getWorld() != pos2.getWorld()
                || pos1.getWorld() != pos3.getWorld() || pos1.getWorld() != player.getWorld()) {
            player.sendMessage("[������ͼ] ѡ��λ����Ч��");
            return;
        }
        // ɾ��ѡ������򲢿�ʼִ����ͼ����
        BlueprintUtil.delSelected(player);
        int[] xyzRange = BlueprintUtil.getRange(pos1, pos2);
        World world = pos1.getWorld();

        // �첽����
        asyncCalculation(player, validItem, world, xyzRange, pos3.clone());
    }

    private static void asyncCalculation(Player player, List<ItemStack> validItem, World world, int[] xyzRange, Location copyPos) {
        // �����첽������
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PluginCore.getInstance(), () -> {
            Location playerPos = player.getEyeLocation().clone();
            Map<Material, List<ItemStack>> materialListMap = validItem.stream().collect(Collectors.groupingBy(i -> i.getType()));
            Map<Block, Block> needProcessBlockMap = new HashMap<>();
            // ������ת�Ƕ�
            double[][] rotateMatrix = customAngle(copyPos, playerPos);
            copyPos.add(-0.5, 0, -0.5);
            for (int x = xyzRange[0]; x <= xyzRange[1]; x++) {
                for (int y = xyzRange[2]; y <= xyzRange[3]; y++) {
                    for (int z = xyzRange[4]; z <= xyzRange[5]; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (BlueprintUtil.isValueCopyBlock(block) && materialListMap.containsKey(block.getType())) {
                            // ����λ��ƫ����
                            Location subtract = customMove(copyPos, playerPos, block, rotateMatrix);
                            Block target = subtract.getBlock();
                            if (target.isEmpty() || target.isPassable()) {
                                Iterator<ItemStack> itemStackIterator = materialListMap.get(block.getType()).iterator();
                                ItemStack index = itemStackIterator.next();
                                while (index.getAmount() <= 0 && itemStackIterator.hasNext()) {
                                    index = itemStackIterator.next();
                                }
                                if (index.getAmount() > 0) {
                                    //�첽��������Ч��
                                    needProcessBlockMap.put(target, block);
                                    index.setAmount(index.getAmount() - 1);
                                } else {
                                    materialListMap.remove(block.getType());
                                }
                            }
                        }
                        if (materialListMap.isEmpty()) {
                            BlueprintUtil.syncBuildFromBlock(needProcessBlockMap);
                            player.sendMessage("[������ͼ] ���ϲ��㣡�޷�ȫ������");
                            return;
                        }
                    }
                }
            }

            // ���
            BlueprintUtil.syncBuildFromBlock(needProcessBlockMap);
            // �黹����
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
            // 0 ��
            return new double[][]{{1, 0}, {0, 1}};
        } else if ((copyV.getX() + playerV.getX()) == 0 && (copyV.getZ() + playerV.getZ()) == 0) {
            // 180 ��
            return new double[][]{{-1, 0}, {0, -1}};
        } else if ((copyV.getX() * playerV.getZ() - copyV.getZ() * playerV.getX()) > 0){
            // +90 ��
            return new double[][]{{0, -1}, {1, 0}};
        } else {
            // -90 ��
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