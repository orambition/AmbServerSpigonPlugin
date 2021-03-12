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
    // ���ǹ�����ͬ���ܵ����ã����ļ������洢�����������ò�����Ҳ�����ӣ�Ŀǰ����һ��Ҳû����
    /** ������������� **/
    public static String tpBookTitle;
    public static String tpBookMenuTitle;
    public static int tpBookPageMax;// Ĭ�Ͽ������ĵĵ�����û��ͨ��ʱ�����Ĵ˵���
    public static Material tpBookItem;
    public static Material publicTpItem;
    public static Material privateFastTpItem;
    public static Material privateTpItem;
    public static Material deadTpItem;
    public static Material deadInfoItem; // �������������ƵĽ���
    public static Material switchTpItem;
    public static Material switchOffTpItem;
    public static Material addPrivateTpItem;
    public static Material beforeTpItem; // �������������ƵĽ���
    public static Material tpBookCurrencyItem;// �������ĵ�ͨ��
    public static String tpBookCurrencyItemName;// �������ĵ�ͨ��
    public static int deadTpMax;
    public static int privateTpMax;
    public static int publicTpMax;
    public static int tpBookTpPrice;// ��������ͨ��������
    public static int tpBookAddTpPrice;// �������͵�����ͨ���Ļ���������ָ������

    /** �״�������� **/
    public static Material radarItem;
    private static final String RADARITEM_PATH = "radar.item";
    public static String radarName;
    private static final String RADARNAME_PATH = "radar.name";
    public static int raderBatteryMax;// �״����ʹ�ô���
    private static final String RADERBATTERYMAX_PATH = "radar.rulu.max.usecount";
    public static int raderBatteryPre;// �״�ÿ����������
    private static final String RADERBATTERYPRE_PATH = "radar.rulu.userprice";
    public static int raderFoundRangeMax;// �״���������뾶
    private static final String RADER_FOUND_RANGR_MAX_PATH = "radar.rulu.max.found";

    /** ������ͼ���� **/
    public static Material blueprintSelectorItem;
    public static int blueprintSelectorMaxRange;
    public static String blueprintBuildItemPutName;
    public static String blueprintCopyItemPutName;
    public static int blueprintBreakItemNeedExpCount;
    public static String blueprintModeSelectMenu;
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
        /** ������ Ĭ������ **/
        pluginConfig.addDefault("tpb.book.item", Material.ENCHANTED_BOOK.toString());
        pluginConfig.addDefault("tpb.book.title", ChatColor.BOLD + "������");
        pluginConfig.addDefault("tpb.book.canusecount", 10);
        pluginConfig.addDefault("tpb.book.menu.title",ChatColor.BOLD + "˼�����Ĵ�����");
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
        pluginConfig.addDefault("tpb.book.currency.name","�̱�ʯ");
        pluginConfig.addDefault("tpb.book.currency.tpprice",2);
        pluginConfig.addDefault("tpb.book.currency.addtpbase",2);

        /** �״� Ĭ������ **/
        pluginConfig.addDefault(RADARITEM_PATH, Material.COMPASS.toString());
        pluginConfig.addDefault(RADARNAME_PATH, "�����״�");
        pluginConfig.addDefault(RADERBATTERYMAX_PATH, 10);
        pluginConfig.addDefault(RADERBATTERYPRE_PATH, 6);
        pluginConfig.addDefault(RADER_FOUND_RANGR_MAX_PATH, 4);

        /** ������ͼ Ĭ������ **/
        pluginConfig.addDefault("blueprint.selector.item", Material.WOODEN_AXE.toString());
        pluginConfig.addDefault("blueprint.selector.range.max", 64);
        pluginConfig.addDefault("blueprint.build.put.view.name", "������ͼ - �����[����]");
        pluginConfig.addDefault("blueprint.copy.put.view.name", "������ͼ - �����ճ�������[����]");
        pluginConfig.addDefault("blueprint.break.need.exp.count", 2);
        pluginConfig.addDefault("blueprint.mod.select.menu", "������ͼ - ģʽѡ��");
        pluginConfig.addDefault("blueprint.batch.put.max.count", 4);

        pluginConfig.options().copyDefaults(true);
        plugin.saveConfig();

        gameRuleConfig = new GameRuleConfig(pluginConfig);
        /** ������ ���û�ȡ **/
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
        tpBookCurrencyItemName = pluginConfig.getString("tpb.book.currency.name","�̱�ʯ");

        /** �״� ���û�ȡ **/
        radarItem = Material.getMaterial(pluginConfig.getString(RADARITEM_PATH));
        radarName = pluginConfig.getString(RADARNAME_PATH);
        raderBatteryMax = pluginConfig.getInt(RADERBATTERYMAX_PATH, 10);
        raderBatteryPre = pluginConfig.getInt(RADERBATTERYPRE_PATH, 6);
        raderFoundRangeMax = pluginConfig.getInt(RADER_FOUND_RANGR_MAX_PATH, 4);

        /** ������ͼ ���û�ȡ **/
        blueprintSelectorItem = Material.getMaterial(pluginConfig.getString("blueprint.selector.item"));
        blueprintSelectorMaxRange = pluginConfig.getInt("blueprint.selector.range.max", 64);
        blueprintBuildItemPutName = pluginConfig.getString("blueprint.build.put.view.name");
        blueprintCopyItemPutName = pluginConfig.getString("blueprint.copy.put.view.name");
        blueprintBreakItemNeedExpCount = pluginConfig.getInt("blueprint.break.need.exp.count", 2);
        blueprintModeSelectMenu = pluginConfig.getString("blueprint.mod.select.menu");
        blueprintBatchPutMaxCount = pluginConfig.getInt("blueprint.batch.put.max.count", 4);
    }
    /** ������ ���ݴ洢 **/
    private static void getTpbSaveData(JavaPlugin plugin){
        tpbSaveDataFile = new File(plugin.getDataFolder(),"tpbSaveData.yml");
        tpbSaveData = YamlConfiguration.loadConfiguration(tpbSaveDataFile);
        saveTpbSaveData();
    }
    public static void saveTpbSaveData(){
        try {
            tpbSaveData.save(tpbSaveDataFile);
        } catch (IOException e) {
            logger.info("tpbSaveData ERROR, ���������ݱ���ʧ��:"+e.getMessage());
        }
    }
    /** Ȩ�� ���ݴ洢
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
            logger.info("permissionSaveData ERROR, Ȩ�����ݱ���ʧ��:"+e.getMessage());
        }
    }
}
