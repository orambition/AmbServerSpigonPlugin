package amb.server.plugin.init;

import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfig {

    public static FileConfiguration configuration;

    public static void init(FileConfiguration config){
        configuration = config;
        setDefaultConfig();
    }
    private static void setDefaultConfig(){
        configuration.addDefault("tpb.title","tpbook");
        configuration.options().copyDefaults(true);
    }
}
