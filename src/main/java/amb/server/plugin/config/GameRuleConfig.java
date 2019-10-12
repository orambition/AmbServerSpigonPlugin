package amb.server.plugin.config;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class GameRuleConfig {
    Logger logger = PluginLogger.getLogger("AmbSP");
    // ¹ÖÎïÆÆ»µ·½¿é
    private boolean mobGriefing;
    // »ðÂûÑÓ
    private boolean doFireTick;

    public GameRuleConfig(boolean mobGriefing, boolean doFireTick) {
        this.mobGriefing = mobGriefing;
        this.doFireTick = doFireTick;
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
