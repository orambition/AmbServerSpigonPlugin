package amb.server.plugin.core;

import amb.server.plugin.command.TpBookCommand;
import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.listener.ManageListener;
import amb.server.plugin.listener.TpBookListener;
import amb.server.plugin.service.tpb.TpBookItem;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginCore extends JavaPlugin {
    private static PluginCore instance;
    public static PluginCore getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        System.out.println("[AmbSP]-启动中……");
        instance = this;
        PluginConfig.init(this);
        TpBookItem.addRecipe(this);
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new TpBookListener(), this);
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
        System.out.println("[AmbSP]-关闭中……");
    }

}
