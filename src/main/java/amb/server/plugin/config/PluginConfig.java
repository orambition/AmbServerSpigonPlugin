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
    public static Material tpBookCurrencyItem;// �������ĵ�ͨ��
    public static String tpBookCurrencyItemName;// �������ĵ�ͨ��
    public static int deadTpMax;
    public static int privateTpMax;
    public static int publicTpMax;
    public static int tpBookTpPrice;// ��������ͨ��������
    public static int tpBookAddTpPrice;// �������͵�����ͨ���Ļ���������ָ������
    public static void init(JavaPlugin plugin){
        getPluginConfig(plugin);
        getTpbSaveData(plugin);
    }
    private static void getPluginConfig(JavaPlugin plugin){
        pluginConfig = plugin.getConfig();
        pluginConfig.addDefault("server.manage.gamerule.mobGriefing",false);
        pluginConfig.addDefault("server.manage.gamerule.doFireTick",false);
        pluginConfig.addDefault("tpb.book.item", Material.ENCHANTED_BOOK.toString());
        pluginConfig.addDefault("tpb.book.title", ChatColor.RESET + "" + ChatColor.BOLD + "������");
        pluginConfig.addDefault("tpb.book.canusecount", 10);
        pluginConfig.addDefault("tpb.book.menu.title",ChatColor.BOLD + "˼�����Ĵ�����");
        pluginConfig.addDefault("tpb.book.menu.item.publictp",Material.END_CRYSTAL.toString());
        pluginConfig.addDefault("tpb.book.menu.item.privatefasttp",Material.JUNGLE_SIGN.toString());
        pluginConfig.addDefault("tpb.book.menu.item.privatetp",Material.OAK_SIGN.toString());
        pluginConfig.addDefault("tpb.book.menu.item.deadtp",Material.TOTEM_OF_UNDYING.toString());
        pluginConfig.addDefault("tpb.book.menu.item.deadinfo",Material.GOLDEN_APPLE.toString());
        pluginConfig.addDefault("tpb.book.menu.item.switchtp",Material.LEVER.toString());
        pluginConfig.addDefault("tpb.book.menu.item.switchofftp",Material.BARRIER.toString());
        pluginConfig.addDefault("tpb.book.menu.item.addprivatetp",Material.BIRCH_SIGN.toString());
        pluginConfig.addDefault("tpb.book.rulu.max.deadtp",6);
        pluginConfig.addDefault("tpb.book.rulu.max.privatetp",9);
        pluginConfig.addDefault("tpb.book.rulu.max.publictp",9);
        pluginConfig.addDefault("tpb.book.currency.item",Material.EMERALD.toString());
        pluginConfig.addDefault("tpb.book.currency.name","�̱�ʯ");
        pluginConfig.addDefault("tpb.book.currency.tpprice",2);
        pluginConfig.addDefault("tpb.book.currency.addtpbase",2);
        pluginConfig.options().copyDefaults(true);
        plugin.saveConfig();
        gameRuleConfig = new GameRuleConfig(pluginConfig.getBoolean("server.manage.gamerule.mobGriefing",false),
                pluginConfig.getBoolean("server.manage.gamerule.doFireTick",false));
        tpBookTitle = pluginConfig.getString("tpb.book.title");
        tpBookMenuTitle = pluginConfig.getString("tpb.book.menu.title");
        tpBookPageMax = pluginConfig.getInt("tpb.book.canusecount",10);
        tpBookItem = Material.getMaterial(pluginConfig.getString("tpb.book.item"));
        publicTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.publictp"));
        privateFastTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.privatefasttp"));
        privateTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.privatetp"));
        deadTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.deadinfo"));
        deadInfoItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.deadtp"));
        switchTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.switchtp"));
        switchOffTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.switchofftp"));
        addPrivateTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.addprivatetp"));
        publicTpMax = pluginConfig.getInt("tpb.book.rulu.max.publictp",9);
        privateTpMax = pluginConfig.getInt("tpb.book.rulu.max.privatetp",9);
        deadTpMax = pluginConfig.getInt("tpb.book.rulu.max.deadtp",6);
        tpBookCurrencyItem = Material.getMaterial(pluginConfig.getString("tpb.book.currency.item"));
        tpBookTpPrice = pluginConfig.getInt("tpb.book.currency.tpprice",2);
        tpBookAddTpPrice = pluginConfig.getInt("tpb.book.currency.addtpbase",2);
        tpBookCurrencyItemName = pluginConfig.getString("tpb.book.currency.name","�̱�ʯ");
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
