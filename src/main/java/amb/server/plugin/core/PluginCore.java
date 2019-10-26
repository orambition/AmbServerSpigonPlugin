package amb.server.plugin.core;

import amb.server.plugin.command.TpBookCommand;
import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.listener.ManageListener;
import amb.server.plugin.listener.PlayerListener;
import amb.server.plugin.service.radar.RadarItem;
import amb.server.plugin.service.tpb.TpBookItem;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class PluginCore extends JavaPlugin {
    private final Logger logger = PluginLogger.getLogger("Ambition");
    private static PluginCore instance;
    public static PluginCore getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        logger.info("[AmbSP]-启动中……");
        instance = this;
        PluginConfig.init(this);
        // 注册传送书的合成表
        TpBookItem.addRecipe(this);
        // 注册万能雷达合成表
        RadarItem.addRecipe(this);
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new ManageListener(), this);
        this.getCommand("tpb").setExecutor(new TpBookCommand());
        this.getCommand("tpbrequest").setExecutor(new TpBookCommand());

        PluginConfig.gameRuleConfig.init(this.getServer().getWorld("world"));
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onDisable() {
        logger.info("[AmbSP]-关闭中……");
    }

}
