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

import java.util.*;
import java.util.stream.Collectors;

import static amb.server.plugin.service.utils.PlayerUtils.PLAYER_BLUEPRINT_SELECT;

/**
 * @author zhangrenjing
 * created on 2021/3/12
 */
public class FillingMode {

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
            Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.blueprintBuildItemPutName);
            player.openInventory(inventory);
        }, 30);
        GUIUtils.sendMsg(player, "����ѡ��ɹ�������뽨�����");
        player.sendMessage("[������ͼ] ����ѡ��ɹ�������뽨��[����]");
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
        int[] xyzRange = BlueprintUtil.getRange(pos1, pos2);
        World world = pos1.getWorld();

        // ���
        asyncBuild(player, validItem, world, xyzRange);
    }

    /**
     * �첽����
     *
     * @param player
     * @param validItem
     * @param world
     * @param xyzRange
     */
    private static void asyncBuild(Player player, List<ItemStack> validItem, World world, int[] xyzRange) {
        // �����첽������
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PluginCore.getInstance(), () -> {
            Map<Block, Material> needProcessBlockMap = new HashMap<>();
            Iterator<ItemStack> itemStackIterator = validItem.iterator();
            ItemStack index = itemStackIterator.next();
            for (int x = xyzRange[0]; x <= xyzRange[1]; x++) {
                for (int y = xyzRange[2]; y <= xyzRange[3]; y++) {
                    for (int z = xyzRange[4]; z <= xyzRange[5]; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.isEmpty() || block.isPassable()) {
                            while (index.getAmount() <= 0 && itemStackIterator.hasNext()) {
                                index = itemStackIterator.next();
                            }
                            if (index.getAmount() > 0) {
                                //�첽������Ч�� block.setType(index.getType());
                                needProcessBlockMap.put(block, index.getType());
                                index.setAmount(index.getAmount() - 1);
                            } else {
                                player.sendMessage("[������ͼ] ���ϲ��㣡�޷�ȫ�����");
                                BlueprintUtil.syncBuild(needProcessBlockMap);
                                return;
                            }
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

}
