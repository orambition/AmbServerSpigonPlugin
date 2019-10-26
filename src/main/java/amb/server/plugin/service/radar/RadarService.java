package amb.server.plugin.service.radar;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.utils.GUIUtils;
import amb.server.plugin.service.utils.map.MapUtil;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static amb.server.plugin.config.ConstantConfig.*;

public class RadarService {
    private static final int scanRange = 6; // 正方形扫描 的边长 6区块
    private static final int chunkSize = 16; // 区块大小
    private static final Vector DOWN_DIR = new Vector(0,0,chunkSize);
    private static final Vector LEFT_DIR = new Vector(-chunkSize,0,0);
    private static final Vector UP_DIR = new Vector(0,0,-chunkSize);
    private static final Vector RIGHT_DIR = new Vector(chunkSize,0,0);
    private static Vector tempVector;
    /**
     * 右键使用雷达
     * @param player
     * @param radar
     */
    public static void user(Player player, ItemStack radar){
        Material targetMaterial = RadarItem.getRadarTarget(radar);
        if (targetMaterial == null){
            return;
        }
        ItemMeta itemMeta = radar.getItemMeta();
        int batter = itemMeta.getEnchantLevel(Enchantment.DAMAGE_ALL);
        if (batter >= PluginConfig.raderBatteryPre){
            Material material = checkTargetMaterial(targetMaterial);
            if (material == null){
                GUIUtils.sendMsg(player, "样本无效!请按[左键]设置搜索样本");
                return;
            }
            // 扣减能量
            RadarItem.buildRadar(radar, targetMaterial, batter - PluginConfig.raderBatteryPre);
            player.getInventory().setItemInMainHand(radar);
            player.sendMessage("消耗["+PluginConfig.raderBatteryPre+"格]能量");
            // 添加冷却标记
            player.addScoreboardTag(PLAYER_RADAR_COOLING);
            doAsynScan(player, material);
        } else {
            GUIUtils.sendMsg(player, "能量不足"+PluginConfig.raderBatteryPre+"颗！请按[左键]放入绿宝石");
            //player.sendMessage("合成前请取出样本,以避免样本损害!");
        }
    }
    private static Material checkTargetMaterial(Material targetMaterial){
        if (targetMaterial.isAir()){
            return null;
        } else if (targetMaterial.isBlock()){
            return targetMaterial;
        } else {
            String name = targetMaterial.name();
            if (name.contains("_INGOT")){
                return Material.matchMaterial(name.replace("INGOT", "ORE"));
            } else {
                return Material.matchMaterial(name+"_ORE");
            }
        }
    }

    private static void doAsynScan(Player player, Material targetMaterial){
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(),
                () -> {
                    Location playerLocation = player.getLocation();
                    List<Double> rX = new ArrayList<>();
                    List<Double> rZ = new ArrayList<>();
                    GUIUtils.sendMsg(player, "搜索中,请稍后…");
                    int realRange = doScan(playerLocation, targetMaterial, PluginConfig.raderFoundRangeMax, 0, 0, rX, rZ);
                    if (realRange < PluginConfig.raderFoundRangeMax){
                        player.sendMessage(ChatColor.RED + "已找到超过"+PluginConfig.raderBatteryPre+"块样本，搜索已提前结束！");
                    }
                    if (rX.size() > 0 && rX.size() == rZ.size()){
                        // 注意此处有类型转换，实际绘制时为byte类型坐标
                        ItemStack map = MapUtil.buildMap(playerLocation, rX, rZ, realRange * chunkSize);
                        playerLocation.getWorld().dropItem(playerLocation, map);
                    } else {
                        // 扫描结果为空
                        GUIUtils.sendMsg(player, "方圆百格内无搜索目标!");
                    }
        });
        // 3s 删除玩家冷却标记
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PluginCore.getInstance(),
                () -> player.removeScoreboardTag(PLAYER_RADAR_COOLING),
                150L
        );
    }

    /**
     * 区域扫描 以区块为单位
     * @param location 已此位置所在区块为中心
     * @param targetMaterial 目标
     * @param range 扫描范围只支持偶数，单位区块 6 = 6区块
     * @param offsetX 结果偏移量
     * @param offsetZ 结果偏移量
     * @param rX 结果集
     * @param rZ 结果集
     */
    private static int doScan(Location location, Material targetMaterial,int range, double offsetX, double offsetZ,
                               List<Double> rX, List<Double> rZ){
        Location tempLocation = location.clone();
        int temp = tempLocation.getBlockX() % chunkSize;
        int centerOffsetX = -(temp < 0 ? chunkSize+temp : temp);
        temp = tempLocation.getBlockZ() % chunkSize;
        int centerOffsetZ =  -(temp < 0 ? chunkSize+temp : temp);

        boolean first = true;
        // 搜索边长 0 2 4 6 8 10
        int i;
        for (i = 2; i < range; i+=2){
            int tempI = 0;
            int tempF = 0;
            while (tempF < 4){
                scanChunk(tempLocation.getChunk().getChunkSnapshot(), targetMaterial, rX, rZ,
                        offsetX + centerOffsetX, offsetZ + centerOffsetZ);
                if (first){
                    first = false;
                    if (rX.size() >= PluginConfig.raderBatteryPre) {
                        return 1;
                    }
                }
                if (++tempI < i-1){
                    // 继续此方向
                    tempVector = getNowDir(tempF, false);
                } else {
                    // 转向，=3时有特殊处理
                    tempVector = getNowDir(tempF++, true);
                    tempI = 0;
                }
                tempLocation.add(tempVector);
                centerOffsetX += tempVector.getX();
                centerOffsetZ += tempVector.getZ();
            }
            if (rX.size() >= PluginConfig.raderBatteryPre){
                return i;
            }
        }
        return i;
    }

    /**
     * 获取当前方向
     * @param tempF
     * @param turn
     * @return
     */
    private static Vector getNowDir(int tempF, boolean turn){
        // 0下，1左，2上，3右
        switch (tempF){
            case 0:
                return turn ? LEFT_DIR : DOWN_DIR;
            case 1:
                return turn ? UP_DIR : LEFT_DIR;
            case 2:
                return turn ? RIGHT_DIR : UP_DIR;
                // case 3转为时说明当前循环结束，即前往下一循环，此处为向右移动，即与默认值相同
                default:
                    return RIGHT_DIR;
        }
    }

    /**
     * 扫描区块
     * @param chunk 区块
     * @param targetMaterial 目标
     * @param xs 扫描结果x
     * @param zs 扫描结果z
     * @param offsetX 结果偏移量x
     * @param offsetZ 结果偏移量z
     */
    private static void scanChunk(ChunkSnapshot chunk, Material targetMaterial,
                                  List<Double> xs, List<Double> zs,
                                  double offsetX, double offsetZ){
        for (int x = 0; x < 16; x++){
            for (int z = 0; z < 16; z++){
                for (int y = 0; y < chunk.getHighestBlockYAt(x, z); y++){
                    if (chunk.getBlockType(x, y, z).equals(targetMaterial)){
                        xs.add(offsetX + x);
                        zs.add(offsetZ + z);
                        break;
                    }
                }
            }
        }
    }
    /**
     * 左键打开雷达
     * @param player
     * @param item
     */
    public static void open(Player player, ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        int batter = itemMeta.getEnchantLevel(Enchantment.DAMAGE_ALL);

        Inventory inventory = Bukkit.createInventory(null, 27, PluginConfig.radarName);
        int num = 0;
        ItemStack wGlass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemStack rGlass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta itemMeta1 = rGlass.getItemMeta();
        itemMeta1.setDisplayName("样本槽");
        itemMeta1.setLore(Collections.singletonList("仅支持一个样本,请勿防止多个!"));
        rGlass.setItemMeta(itemMeta1);
        ItemStack gGlass = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta itemMeta2 = gGlass.getItemMeta();
        itemMeta2.setDisplayName("能量槽");
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
        inventory.setItem(num++, batter>0 ? emerald : new ItemStack(Material.AIR));
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
     * 设置雷达目标
     * @param player
     * @param newTargetItem
     */
    public static void setTargetAndPower(Player player, ItemStack newTargetItem, ItemStack newPowerItem) {
        Material newTargetMaterial = newTargetItem == null? Material.AIR : newTargetItem.getType();
        ItemStack radar = player.getInventory().getItemInMainHand();
        Material oldTargetMaterial = RadarItem.getRadarTarget(radar);

        boolean needUpdate = false;
        // 此处为空说明不是radar，起到校验作用
        if (oldTargetMaterial != null) {
            if (newTargetItem != null && newTargetItem.getAmount() > 1) {
                newTargetItem.setAmount(newTargetItem.getAmount() -1);
                player.getWorld().dropItem(player.getLocation(), newTargetItem);
                player.sendMessage("样本仅支持1个!多余样本已退还");
            }
            int power = 0;
            if (newPowerItem != null){
                if (newPowerItem.getType().equals(Material.EMERALD)){
                    power = newPowerItem.getAmount();
                } else {
                    player.getWorld().dropItem(player.getLocation(), newPowerItem);
                    player.sendMessage("能量物品仅支持绿宝石!");
                }
            }
            RadarItem.buildRadar(radar, newTargetMaterial, power);
        }

    }
}
