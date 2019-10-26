package amb.server.plugin.config;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class PluginConfig {

    public static FileConfiguration pluginConfig;
    public static FileConfiguration tpbSaveData;
    private static File tpbSaveDataFile;

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

    public static void init(JavaPlugin plugin){
        getPluginConfig(plugin);
        getTpbSaveData(plugin);
    }
    private static void getPluginConfig(JavaPlugin plugin){
        pluginConfig = plugin.getConfig();
        pluginConfig.addDefault(GameRuleConfig.MOB_GRIEFING_PATH,false);
        pluginConfig.addDefault(GameRuleConfig.DO_FIRE_TICK_PATH,false);
        /** ������������� **/
        pluginConfig.addDefault("tpb.book.item", Material.ENCHANTED_BOOK.toString());
        pluginConfig.addDefault("tpb.book.title", ChatColor.RESET + "" + ChatColor.BOLD + "������");
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

        /** �״�������� **/
        pluginConfig.addDefault(RADARITEM_PATH, Material.COMPASS.toString());
        pluginConfig.addDefault(RADARNAME_PATH, "�����״�");
        pluginConfig.addDefault(RADERBATTERYMAX_PATH, 10);
        pluginConfig.addDefault(RADERBATTERYPRE_PATH, 6);
        pluginConfig.addDefault(RADER_FOUND_RANGR_MAX_PATH, 4);

        pluginConfig.options().copyDefaults(true);
        plugin.saveConfig();

        gameRuleConfig = new GameRuleConfig(pluginConfig);
        /** ������������� **/
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

        /** �״�������� **/
        radarItem = Material.getMaterial(pluginConfig.getString(RADARITEM_PATH));
        radarName = pluginConfig.getString(RADARNAME_PATH);
        raderBatteryMax = pluginConfig.getInt(RADERBATTERYMAX_PATH, 10);
        raderBatteryPre = pluginConfig.getInt(RADERBATTERYPRE_PATH, 6);
        raderFoundRangeMax = pluginConfig.getInt(RADER_FOUND_RANGR_MAX_PATH, 4);
    }
    private static void getTpbSaveData(JavaPlugin plugin){
        tpbSaveDataFile = new File(plugin.getDataFolder(),"tpbSaveData.yml");
        tpbSaveData = YamlConfiguration.loadConfiguration(tpbSaveDataFile);
        saveTpbSaveData();
    }
    public static void saveTpbSaveData(){
        try {
            tpbSaveData.save(tpbSaveDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
