package amb.server.plugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class PluginConfig {

    public static FileConfiguration pluginConfig;
    public static FileConfiguration tpbSaveData;

    private static File tpbSaveDataFile;

    public static void init(JavaPlugin plugin){
        getPluginConfig(plugin);
        getTpbSaveData(plugin);
    }
    private static void getPluginConfig(JavaPlugin plugin){
        pluginConfig = plugin.getConfig();
        pluginConfig.addDefault("tpb.book.title","传送书");
        pluginConfig.addDefault("tpb.menu.title","%s的传送书");
        pluginConfig.options().copyDefaults(true);
        plugin.saveConfig();
    }
    private static void getTpbSaveData(JavaPlugin plugin){
        tpbSaveDataFile = new File(plugin.getDataFolder(),"tpbSaveData.yml");
        tpbSaveData = YamlConfiguration.loadConfiguration(tpbSaveDataFile);

        tpbSaveData.set("public","123");
        tpbSaveData.addDefault("def1","123");
        tpbSaveData.addDefault("def2","456");
        tpbSaveData.options().copyDefaults(true);
        saveTpbSaveData();
        //plugin.saveResource("tpbSaveData.yml", false);
    }
    public static void saveTpbSaveData(){
        try {
            tpbSaveData.save(tpbSaveDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
