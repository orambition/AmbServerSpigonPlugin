package amb.server.plugin.config;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class PluginConfig {
    private static final Logger logger = PluginLogger.getLogger("Ambition");
    public static FileConfiguration pluginConfig;

    private static File tpbSaveDataFile;
    public static FileConfiguration tpbSaveData;

    private static File ambPermissionSaveDataFile;
    public static FileConfiguration ambPermissionSaveData;

    public static GameRuleConfig gameRuleConfig;
    // 考虑过将不同功能的配置，分文件单独存储，但发现配置并不多也不复杂，目前放在一起也没问题
    /** 传送书相关配置 **/
    public static String tpBookTitle;
    public static String tpBookMenuTitle;
    public static int tpBookPageMax;// 默认可以消耗的点数，没有通货时将消耗此点数
    public static Material tpBookItem;
    public static Material publicTpItem;
    public static Material privateFastTpItem;
    public static Material privateTpItem;
    public static Material deadTpItem;
    public static Material deadInfoItem; // 传送书死亡机制的介绍
    public static Material switchTpItem;
    public static Material switchOffTpItem;
    public static Material addPrivateTpItem;
    public static Material beforeTpItem; // 传送书死亡机制的介绍
    public static Material tpBookCurrencyItem;// 传送消耗的通货
    public static String tpBookCurrencyItemName;// 传送消耗的通货
    public static int deadTpMax;
    public static int privateTpMax;
    public static int publicTpMax;
    public static int tpBookTpPrice;// 传送消耗通货的数量
    public static int tpBookAddTpPrice;// 新增传送点消耗通货的基础数量，指数增长

    /** 雷达相关配置 **/
    public static Material radarItem;
    private static final String RADARITEM_PATH = "radar.item";
    public static String radarName;
    private static final String RADARNAME_PATH = "radar.name";
    public static int raderBatteryMax;// 雷达最多使用次数
    private static final String RADERBATTERYMAX_PATH = "radar.rulu.max.usecount";
    public static int raderBatteryPre;// 雷达每次消耗数量
    private static final String RADERBATTERYPRE_PATH = "radar.rulu.userprice";
    public static int raderFoundRangeMax;// 雷达最多搜索半径
    private static final String RADER_FOUND_RANGR_MAX_PATH = "radar.rulu.max.found";

    /** 建筑蓝图配置 **/
    public static Material blueprintSelectorItem;
    public static int blueprintSelectorMaxRange;
    public static int blueprintBreakItemNeedExpCount;
    public static int blueprintBatchPutMaxCount;

    public static void init(JavaPlugin plugin){
        getPluginConfig(plugin);
        getTpbSaveData(plugin);
        initPermissionSaveDate(plugin);
    }
    private static void getPluginConfig(JavaPlugin plugin){
        pluginConfig = plugin.getConfig();
        pluginConfig.addDefault(GameRuleConfig.MOB_GRIEFING_PATH,false);
        pluginConfig.addDefault(GameRuleConfig.DO_FIRE_TICK_PATH,false);
        /** 传送书 默认配置 **/
        pluginConfig.addDefault("tpb.book.item", Material.ENCHANTED_BOOK.toString());
        pluginConfig.addDefault("tpb.book.title", ChatColor.BOLD + "传送书");
        pluginConfig.addDefault("tpb.book.canusecount", 10);
        pluginConfig.addDefault("tpb.book.menu.title",ChatColor.BOLD + "思服器的传送书");
        pluginConfig.addDefault("tpb.book.menu.item.publictp",Material.END_CRYSTAL.toString());
        pluginConfig.addDefault("tpb.book.menu.item.privatefasttp",Material.JUNGLE_SIGN.toString());
        pluginConfig.addDefault("tpb.book.menu.item.privatetp",Material.OAK_SIGN.toString());
        pluginConfig.addDefault("tpb.book.menu.item.deadtp",Material.TOTEM_OF_UNDYING.toString());
        pluginConfig.addDefault("tpb.book.menu.item.beforetp",Material.SPECTRAL_ARROW.toString());
        pluginConfig.addDefault("tpb.book.menu.item.deadinfo",Material.GOLDEN_APPLE.toString());
        pluginConfig.addDefault("tpb.book.menu.item.switchtp",Material.LEVER.toString());
        pluginConfig.addDefault("tpb.book.menu.item.switchofftp",Material.BARRIER.toString());
        pluginConfig.addDefault("tpb.book.menu.item.addprivatetp",Material.BIRCH_SIGN.toString());
        pluginConfig.addDefault("tpb.book.rulu.max.deadtp",5);
        pluginConfig.addDefault("tpb.book.rulu.max.privatetp",9);
        pluginConfig.addDefault("tpb.book.rulu.max.publictp",9);
        pluginConfig.addDefault("tpb.book.currency.item",Material.EMERALD.toString());
        pluginConfig.addDefault("tpb.book.currency.name","绿宝石");
        pluginConfig.addDefault("tpb.book.currency.tpprice",2);
        pluginConfig.addDefault("tpb.book.currency.addtpbase",2);

        /** 雷达 默认配置 **/
        pluginConfig.addDefault(RADARITEM_PATH, Material.COMPASS.toString());
        pluginConfig.addDefault(RADARNAME_PATH, "万能雷达");
        pluginConfig.addDefault(RADERBATTERYMAX_PATH, 10);
        pluginConfig.addDefault(RADERBATTERYPRE_PATH, 6);
        pluginConfig.addDefault(RADER_FOUND_RANGR_MAX_PATH, 4);

        /** 建筑蓝图 默认配置 **/
        pluginConfig.addDefault("blueprint.selector.item", Material.WOODEN_AXE.toString());
        pluginConfig.addDefault("blueprint.selector.range.max", 32);
        pluginConfig.addDefault("blueprint.break.need.exp.count", 2);
        pluginConfig.addDefault("blueprint.batch.put.max.count", 4);

        pluginConfig.options().copyDefaults(true);
        plugin.saveConfig();

        gameRuleConfig = new GameRuleConfig(pluginConfig);
        /** 传送书 配置获取 **/
        tpBookTitle = pluginConfig.getString("tpb.book.title");
        tpBookMenuTitle = pluginConfig.getString("tpb.book.menu.title");
        tpBookPageMax = pluginConfig.getInt("tpb.book.canusecount",10);
        tpBookItem = Material.getMaterial(pluginConfig.getString("tpb.book.item"));
        publicTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.publictp"));
        privateFastTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.privatefasttp"));
        privateTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.privatetp"));
        deadTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.deadinfo"));
        deadInfoItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.deadtp"));
        beforeTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.beforetp"));
        switchTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.switchtp"));
        switchOffTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.switchofftp"));
        addPrivateTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.addprivatetp"));
        publicTpMax = pluginConfig.getInt("tpb.book.rulu.max.publictp",9);
        privateTpMax = pluginConfig.getInt("tpb.book.rulu.max.privatetp",9);
        deadTpMax = pluginConfig.getInt("tpb.book.rulu.max.deadtp",5);
        tpBookCurrencyItem = Material.getMaterial(pluginConfig.getString("tpb.book.currency.item"));
        tpBookTpPrice = pluginConfig.getInt("tpb.book.currency.tpprice",2);
        tpBookAddTpPrice = pluginConfig.getInt("tpb.book.currency.addtpbase",2);
        tpBookCurrencyItemName = pluginConfig.getString("tpb.book.currency.name","绿宝石");

        /** 雷达 配置获取 **/
        radarItem = Material.getMaterial(pluginConfig.getString(RADARITEM_PATH));
        radarName = pluginConfig.getString(RADARNAME_PATH);
        raderBatteryMax = pluginConfig.getInt(RADERBATTERYMAX_PATH, 10);
        raderBatteryPre = pluginConfig.getInt(RADERBATTERYPRE_PATH, 6);
        raderFoundRangeMax = pluginConfig.getInt(RADER_FOUND_RANGR_MAX_PATH, 4);

        /** 建筑蓝图 配置获取 **/
        blueprintSelectorItem = Material.getMaterial(pluginConfig.getString("blueprint.selector.item"));
        blueprintSelectorMaxRange = pluginConfig.getInt("blueprint.selector.range.max", 32);
        blueprintBreakItemNeedExpCount = pluginConfig.getInt("blueprint.break.need.exp.count", 2);
        blueprintBatchPutMaxCount = pluginConfig.getInt("blueprint.batch.put.max.count", 4);
    }
    /** 传送书 数据存储 **/
    private static void getTpbSaveData(JavaPlugin plugin){
        tpbSaveDataFile = new File(plugin.getDataFolder(),"tpbSaveData.yml");
        tpbSaveData = YamlConfiguration.loadConfiguration(tpbSaveDataFile);
        saveTpbSaveData();
    }
    public static void saveTpbSaveData(){
        try {
            tpbSaveData.save(tpbSaveDataFile);
        } catch (IOException e) {
            logger.info("tpbSaveData ERROR, 传送书数据保存失败:"+e.getMessage());
        }
    }
    /** 权限 数据存储
     * @param plugin**/
    private static void initPermissionSaveDate(JavaPlugin plugin){
        ambPermissionSaveDataFile = new File(plugin.getDataFolder(), "permission.yml");
        ambPermissionSaveData = YamlConfiguration.loadConfiguration(ambPermissionSaveDataFile);
        savePermissionDate();
    }
    public static void savePermissionDate(){
        try {
            ambPermissionSaveData.save(ambPermissionSaveDataFile);
        } catch (IOException e) {
            logger.info("permissionSaveData ERROR, 权限数据保存失败:"+e.getMessage());
        }
    }
}
