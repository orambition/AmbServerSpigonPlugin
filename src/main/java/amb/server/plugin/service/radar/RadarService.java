package amb.server.plugin.service.radar;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.utils.GUIUtils;
import amb.server.plugin.service.utils.PlayerUtils;
import amb.server.plugin.service.utils.map.MapUtil;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static amb.server.plugin.service.utils.PlayerUtils.PLAYER_RADAR_COOLING;

public class RadarService {
    private static final int chunkSize = 16; // �����С
    private static final Vector DOWN_DIR = new Vector(0, 0, chunkSize);
    private static final Vector LEFT_DIR = new Vector(-chunkSize, 0, 0);
    private static final Vector UP_DIR = new Vector(0, 0, -chunkSize);
    private static final Vector RIGHT_DIR = new Vector(chunkSize, 0, 0);
    private static Vector tempVector;

    /**
     * ���ʹ���״��¼�����
     *
     * @param event
     */
    public static void useRadarEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PermissionConstant.RADAR)) {
            player.sendMessage("��Ȩ��ʹ��!����ϵAmb");
            return;
        }
        Action action = event.getAction();
        if ((action.equals(Action.RIGHT_CLICK_BLOCK) && !event.getClickedBlock().getType().isInteractable()) || action.equals(Action.RIGHT_CLICK_AIR)) {
            // �Ҽ�
            event.setCancelled(true);
            user(player, event.getItem());
        } else if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)) {
            // ���
            event.setCancelled(true);
            open(player, event.getItem());
        }
    }

    /**
     * �Ҽ�ʹ���״�
     *
     * @param player
     * @param radar
     */
    private static void user(Player player, ItemStack radar) {
        if (PlayerUtils.hasMark(player, PLAYER_RADAR_COOLING)) return;

        Material targetMaterial = RadarItem.getRadarTarget(radar);
        if (targetMaterial == null) {
            return;
        }
        ItemMeta itemMeta = radar.getItemMeta();
        int batter = itemMeta.getEnchantLevel(Enchantment.DAMAGE_ALL);
        if (batter >= PluginConfig.raderBatteryPre) {
            Material material = checkTargetMaterial(targetMaterial);
            if (material == null) {
                GUIUtils.sendMsg(player, "������Ч!�밴[���]������������");
                return;
            }
            // �ۼ�����
            RadarItem.buildRadar(radar, targetMaterial, batter - PluginConfig.raderBatteryPre);
            player.getInventory().setItemInMainHand(radar);
            player.sendMessage("����[" + PluginConfig.raderBatteryPre + "��]����");
            // �����ȴ���
            PlayerUtils.mark(player, PLAYER_RADAR_COOLING);
            doAsynScan(player, material);
        } else {
            GUIUtils.sendMsg(player, "��������" + PluginConfig.raderBatteryPre + "�ţ��밴[���]�����̱�ʯ");
            //player.sendMessage("�ϳ�ǰ��ȡ������,�Ա���������!");
        }
    }

    private static Material checkTargetMaterial(Material targetMaterial) {
        if (targetMaterial.isAir()) {
            return null;
        } else if (targetMaterial.isBlock()) {
            return targetMaterial;
        } else {
            String name = targetMaterial.name();
            if (name.contains("_INGOT")) {
                return Material.matchMaterial(name.replace("INGOT", "ORE"));
            } else {
                return Material.matchMaterial(name + "_ORE");
            }
        }
    }

    private static void doAsynScan(Player player, Material targetMaterial) {
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            Location playerLocation = player.getLocation();
            List<Double> rX = new ArrayList<>();
            List<Double> rZ = new ArrayList<>();
            GUIUtils.sendMsg(player, "������,���Ժ�");
            int realRange = doScan(playerLocation, targetMaterial, PluginConfig.raderFoundRangeMax, 0, 0, rX, rZ);
            if (realRange < PluginConfig.raderFoundRangeMax) {
                player.sendMessage(ChatColor.RED + "���ҵ�����" + PluginConfig.raderBatteryPre + "����������������ǰ������");
            }
            if (!rX.isEmpty() && rX.size() == rZ.size()) {
                // ע��˴�������ת����ʵ�ʻ���ʱΪbyte��������
                ItemStack map = MapUtil.buildMap(playerLocation, rX, rZ, realRange * chunkSize);
                playerLocation.getWorld().dropItem(playerLocation, map);
                player.sendMessage(ChatColor.GREEN + "����" + rX.size() + "��Ŀ��\n" + ChatColor.RED + "��ͼ��ǻ��ڲ��ú���ʧ,�뾡��ǰ��Ŀ���!");
            } else {
                // ɨ����Ϊ��
                GUIUtils.sendMsg(player, "��Բ�ٸ���������Ŀ��!");
            }
            // 3s ɾ�������ȴ���
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PluginCore.getInstance(),
                    () -> PlayerUtils.unMark(player, PLAYER_RADAR_COOLING), 300L);
        });
    }

    /**
     * ����ɨ�� ������Ϊ��λ
     *
     * @param location       �Ѵ�λ����������Ϊ����
     * @param targetMaterial Ŀ��
     * @param range          ɨ�跶Χֻ֧��ż������λ���� 6 = 6����
     * @param offsetX        ���ƫ����
     * @param offsetZ        ���ƫ����
     * @param rX             �����
     * @param rZ             �����
     */
    private static int doScan(Location location, Material targetMaterial, int range, double offsetX, double offsetZ,
                              List<Double> rX, List<Double> rZ) {
        Location tempLocation = location.clone();
        int temp = tempLocation.getBlockX() % chunkSize;
        int centerOffsetX = -(temp < 0 ? chunkSize + temp : temp);
        temp = tempLocation.getBlockZ() % chunkSize;
        int centerOffsetZ = -(temp < 0 ? chunkSize + temp : temp);

        boolean first = true;
        // �����߳� 0 2 4 6 8 10
        int i;
        for (i = 2; i < range; i += 2) {
            int tempI = 0;
            int tempF = 0;
            while (tempF < 4) {
                scanChunk(tempLocation.getChunk().getChunkSnapshot(), targetMaterial, rX, rZ,
                        offsetX + centerOffsetX, offsetZ + centerOffsetZ);
                if (first) {
                    first = false;
                    if (rX.size() >= PluginConfig.raderBatteryPre) {
                        return 1;
                    }
                }
                if (++tempI < i - 1) {
                    // �����˷���
                    tempVector = getNowDir(tempF, false);
                } else {
                    // ת��=3ʱ�����⴦��
                    tempVector = getNowDir(tempF++, true);
                    tempI = 0;
                }
                tempLocation.add(tempVector);
                centerOffsetX += tempVector.getX();
                centerOffsetZ += tempVector.getZ();
            }
            if (rX.size() >= PluginConfig.raderBatteryPre) {
                return i;
            }
        }
        return i;
    }

    /**
     * ��ȡ��ǰ����
     *
     * @param tempF
     * @param turn
     * @return
     */
    private static Vector getNowDir(int tempF, boolean turn) {
        // 0�£�1��2�ϣ�3��
        switch (tempF) {
            case 0:
                return turn ? LEFT_DIR : DOWN_DIR;
            case 1:
                return turn ? UP_DIR : LEFT_DIR;
            case 2:
                return turn ? RIGHT_DIR : UP_DIR;
            // case 3תΪʱ˵����ǰѭ����������ǰ����һѭ�����˴�Ϊ�����ƶ�������Ĭ��ֵ��ͬ
            default:
                return RIGHT_DIR;
        }
    }

    /**
     * ɨ������
     *
     * @param chunk          ����
     * @param targetMaterial Ŀ��
     * @param xs             ɨ����x
     * @param zs             ɨ����z
     * @param offsetX        ���ƫ����x
     * @param offsetZ        ���ƫ����z
     */
    private static void scanChunk(ChunkSnapshot chunk, Material targetMaterial,
                                  List<Double> xs, List<Double> zs,
                                  double offsetX, double offsetZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < chunk.getHighestBlockYAt(x, z); y++) {
                    if (chunk.getBlockType(x, y, z).equals(targetMaterial)) {
                        xs.add(offsetX + x);
                        zs.add(offsetZ + z);
                        x++;
                        z++;
                        break;
                    }
                }
            }
        }
    }

    /**
     * ������״�
     *
     * @param player
     * @param item
     */
    private static void open(Player player, ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        int batter = itemMeta.getEnchantLevel(Enchantment.DAMAGE_ALL);

        Inventory inventory = Bukkit.createInventory(null, 27, PluginConfig.radarName);
        int num = 0;
        ItemStack wGlass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemStack rGlass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta itemMeta1 = rGlass.getItemMeta();
        itemMeta1.setDisplayName("������");
        itemMeta1.setLore(Collections.singletonList("��֧��һ������,�����ֹ���!"));
        rGlass.setItemMeta(itemMeta1);
        ItemStack gGlass = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta itemMeta2 = gGlass.getItemMeta();
        itemMeta2.setDisplayName("������");
        gGlass.setItemMeta(itemMeta2);
        ItemStack emerald = new ItemStack(Material.EMERALD, batter);
        // 1
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, rGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, gGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, wGlass);
        // 2
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, rGlass);
        inventory.setItem(num++, new ItemStack(RadarItem.getRadarTarget(item)));
        inventory.setItem(num++, rGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, gGlass);
        inventory.setItem(num++, batter > 0 ? emerald : new ItemStack(Material.AIR));
        inventory.setItem(num++, gGlass);
        inventory.setItem(num++, wGlass);
        // 3
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, rGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, gGlass);
        inventory.setItem(num++, wGlass);
        inventory.setItem(num++, wGlass);

        player.openInventory(inventory);
    }

    /**
     * �����״�Ŀ��
     *
     * @param player
     * @param newTargetItem
     */
    public static void setTargetAndPower(Player player, ItemStack newTargetItem, ItemStack newPowerItem) {
        Material newTargetMaterial = newTargetItem == null ? Material.AIR : newTargetItem.getType();
        ItemStack radar = player.getInventory().getItemInMainHand();
        Material oldTargetMaterial = RadarItem.getRadarTarget(radar);

        boolean needUpdate = false;
        // �˴�Ϊ��˵������radar����У������
        if (oldTargetMaterial != null) {
            if (newTargetItem != null && newTargetItem.getAmount() > 1) {
                newTargetItem.setAmount(newTargetItem.getAmount() - 1);
                player.getWorld().dropItem(player.getLocation(), newTargetItem);
                player.sendMessage("������֧��1��!�����������˻�");
            }
            int power = 0;
            if (newPowerItem != null) {
                if (newPowerItem.getType().equals(Material.EMERALD)) {
                    power = newPowerItem.getAmount();
                } else {
                    player.getWorld().dropItem(player.getLocation(), newPowerItem);
                    player.sendMessage("������Ʒ��֧���̱�ʯ!");
                }
            }
            RadarItem.buildRadar(radar, newTargetMaterial, power);
        }

    }
}
