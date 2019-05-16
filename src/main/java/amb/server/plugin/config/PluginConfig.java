package amb.server.plugin.config;

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
    public static String tpBookTitle;
    public static String tpBookMenuTitle;
    public static Material publicTpItem;
    public static Material privateTpItem;

    public static void init(JavaPlugin plugin){
        getPluginConfig(plugin);
        getTpbSaveData(plugin);
    }
    private static void getPluginConfig(JavaPlugin plugin){
        pluginConfig = plugin.getConfig();
        pluginConfig.addDefault("tpb.book.title","������");
        pluginConfig.addDefault("tpb.book.menu.title","%s�Ĵ�����");
        pluginConfig.addDefault("tpb.book.menu.item.publictp",Material.RED_BANNER.toString());
        pluginConfig.addDefault("tpb.book.menu.item.privatetp",Material.BLUE_BANNER.toString());
        pluginConfig.options().copyDefaults(true);
        plugin.saveConfig();
        tpBookTitle = pluginConfig.getString("tpb.book.title");
        tpBookMenuTitle = pluginConfig.getString("tpb.book.menu.title");
        publicTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.publictp"));
        privateTpItem = Material.getMaterial(pluginConfig.getString("tpb.book.menu.item.privatetp"));
    }
    private static void getTpbSaveData(JavaPlugin plugin){
        tpbSaveDataFile = new File(plugin.getDataFolder(),"tpbSaveData.yml");
        tpbSaveData = YamlConfiguration.loadConfiguration(tpbSaveDataFile);
        /*tpbSaveData.set("public","123");
        tpbSaveData.addDefault("def1","123");
        tpbSaveData.addDefault("def2","456");
        tpbSaveData.options().copyDefaults(true);*/
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
