package amb.server.plugin.config;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class GameRuleConfig {
    private final Logger logger = PluginLogger.getLogger("Ambition");

    public static final String MOB_GRIEFING_PATH = "server.manage.gamerule.mobGriefing";
    public static final String DO_FIRE_TICK_PATH = "server.manage.gamerule.doFireTick";

    // ¹ÖÎïÆÆ»µ·½¿é
    private boolean mobGriefing;
    // »ðÂûÑÓ
    private boolean doFireTick;

    public GameRuleConfig(FileConfiguration pluginConfig) {
        this.mobGriefing = pluginConfig.getBoolean(MOB_GRIEFING_PATH,false);
        this.doFireTick = pluginConfig.getBoolean(DO_FIRE_TICK_PATH,false);
    }

    public void init(World world) {
        if (null == world){
            return;
        }
        world.setGameRule(GameRule.MOB_GRIEFING, mobGriefing);
        logger.info(world.getName()+":GameRule:MOB_GRIEFING="+mobGriefing);
        world.setGameRule(GameRule.DO_FIRE_TICK, doFireTick);
        logger.info(world.getName()+":GameRule:DO_FIRE_TICK="+doFireTick);
    }

    public boolean isMobGriefing() {
        return mobGriefing;
    }

    public void setMobGriefing(boolean mobGriefing) {
        this.mobGriefing = mobGriefing;
    }

    public boolean isDoFireTick() {
        return doFireTick;
    }

    public void setDoFireTick(boolean doFireTick) {
        this.doFireTick = doFireTick;
    }
}
