package amb.server.plugin.config;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class GameRuleConfig {
    // �����ƻ�����
    private boolean mobGriefing;
    // ������
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
        System.out.println(world.getName()+":GameRule:MOB_GRIEFING="+mobGriefing);
        world.setGameRule(GameRule.DO_FIRE_TICK, doFireTick);
        System.out.println(world.getName()+":GameRule:DO_FIRE_TICK="+doFireTick);
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
