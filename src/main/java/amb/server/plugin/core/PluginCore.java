package amb.server.plugin.core;

import amb.server.plugin.command.PermissionCommand;
import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.listener.ManageListener;
import amb.server.plugin.listener.PlayerListener;
import amb.server.plugin.service.permission.PermissionDataService;
import amb.server.plugin.service.permission.PermissionService;
import amb.server.plugin.service.radar.RadarItem;
import amb.server.plugin.service.tpb.TpBookItem;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class PluginCore extends JavaPlugin {
    private final Logger logger = PluginLogger.getLogger("Ambition");
    private static final HashMap<UUID, PermissionAttachment> permissionMap = new HashMap<>();
    private static PluginCore instance;
    public static PluginCore getInstance() {
        return instance;
    }

    public static HashMap<UUID, PermissionAttachment> getPermissionMap() {
        return permissionMap;
    }

    @Override
    public void onEnable() {
        logger.info("[AmbSP]-�����С���");
        instance = this;
        PluginConfig.init(this);
        // ע�ᴫ����ĺϳɱ�
        TpBookItem.addRecipe(this);
        // ע�������״�ϳɱ�
        RadarItem.addRecipe(this);

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new ManageListener(), this);

        this.getCommand("amb").setExecutor(new PermissionCommand());

        PluginConfig.gameRuleConfig.init(this.getServer().getWorld("world"));
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onDisable() {
        logger.info("[AmbSP]-�ر��С���");
        permissionMap.clear();
    }

}
