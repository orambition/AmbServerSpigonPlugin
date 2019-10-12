package amb.server.plugin.tools;

import org.bukkit.entity.Player;

public enum PlayerAnimation {
    ARM_SWING(0),
    HURT(1),
    EAT_FOOD(2),
    ARM_SWING_OFFHAND(3),
    CRIT(4),
    MAGIC_CRIT(5),
    SIT(-1),
    SLEEP(-1),
    SNEAK(-1),
    START_ELYTRA(-1),
    START_USE_MAINHAND_ITEM(-1),
    START_USE_OFFHAND_ITEM(-1),
    STOP_SITTING(-1),
    STOP_SLEEPING(-1),
    STOP_SNEAKING(-1),
    STOP_USE_ITEM(-1);

    int code;

    PlayerAnimation(int code) {
        this.code = code;
    }

    public void play(Player player, int radius) {
        NMSUtil.playAnimation(this, player, radius);
    }

    public int getCode() {
        return code;
    }
}
