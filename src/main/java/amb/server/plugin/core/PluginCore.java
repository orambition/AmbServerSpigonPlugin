package amb.server.plugin.core;

import amb.server.plugin.command.TpBookCommand;
import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.listener.TpBookListener;
import amb.server.plugin.service.tpb.TpBookItem;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginCore extends JavaPlugin {
    private static PluginCore instance;

    @Override
    public void onEnable() {
        System.out.println("[AmbSP]-启动中……");
        instance = this;
        PluginConfig.init(this);
        TpBookItem.addRecipe(this);
        this.getServer().getPluginManager().registerEvents(new TpBookListener(), this);
        this.getCommand("tpb").setExecutor(new TpBookCommand());
        this.getCommand("tpbrequest").setExecutor(new TpBookCommand());
    }

    public static PluginCore getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onDisable() {
        System.out.println("[AmbSP]-关闭中……");
    }

}
