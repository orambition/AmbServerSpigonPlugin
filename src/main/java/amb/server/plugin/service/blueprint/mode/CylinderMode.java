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
 * Բ�� ģʽ
 */
public class CylinderMode {
    public static final String BLUEPRINT_CYLINDER_PUT_MENU = "������ͼ - �����Բ���Ľ������[����]";

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
     * ���Ԥ��������У�飬�򿪲���/���������棬
     * ����ҳ��ر��¼�����ʼ���/�ƻ�
     *
     * @param player
     * @param pos1
     * @param pos2
     */
    public static void openMenu(Player player, Location pos1, Location pos2) {
        // �Ӵ�������ͬʱֻ��ִ��һ������
        PlayerUtils.mark(player, PLAYER_BLUEPRINT_SELECT);
        // ��������
        ParticleUtils.drawLineTimer(pos1, pos2);

        // ����, ��������ǵ�ǰ�߳���ʱִ�У�����������"�첽"�ģ������޷����첽�߳��д򿪣���ʱ����Ϊ������Ҷ�����ʾ
        Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
            Inventory inventory = Bukkit.createInventory(null, 54, BLUEPRINT_CYLINDER_PUT_MENU);
            player.openInventory(inventory);
        }, 30);
        GUIUtils.sendMsg(player, "����ѡ��ɹ�������뽨�����");
        //player.sendMessage("[������ͼ] ����ѡ��ɹ�������뽨��[����]");
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
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || pos1.getWorld() != pos2.getWorld()) {
            player.sendMessage("[������ͼ] ѡ��λ����Ч��");
            return;
        }
        // ɾ��ѡ������򲢿�ʼִ����ͼ����
        BlueprintUtil.delSelected(player);
        // ���
        asyncBuild(player, validItem, pos1, pos2);
    }

    /**
     * �첽����
     *
     * @param player
     * @param validItem
     * @param pos1      Բ��
     * @param pos2      �뾶 + �߶�
     */
    private static void asyncBuild(Player player, List<ItemStack> validItem, Location pos1, Location pos2) {
        // �����첽������
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PluginCore.getInstance(), () -> {
            Map<Block, Material> needProcessBlockMap = new HashMap<>();
            Iterator<ItemStack> itemStackIterator = validItem.iterator();
            ItemStack index = itemStackIterator.next();
            List<Location> roundXZ = getCylinderBlockLocation(pos1, pos2);
            // �߶�
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
                            player.sendMessage("[������ͼ] ���ϲ��㣡�޷���ɽ���");
                            BlueprintUtil.syncBuild(needProcessBlockMap);
                            return;
                        }
                    }
                }
            }

            // ���
            BlueprintUtil.syncBuild(needProcessBlockMap);
            // �黹����
            List<ItemStack> backList = validItem.stream().filter(i -> i.getAmount() > 0).collect(Collectors.toList());
            if (!backList.isEmpty()) {
                BlueprintUtil.syncBackBuildItem(player, backList);
            }
        });
    }

    private static List<Location> getCylinderBlockLocation(Location pos1, Location pos2) {
        // �뾶 �� ƽ��
        double radiusPow = Math.pow(pos2.getX() - pos1.getX(), 2) + Math.pow(pos2.getZ() - pos1.getZ(), 2);
        // �뾶
        int radius = (int) Math.round(Math.sqrt(radiusPow));
        // ����Բ�ܷ���
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
