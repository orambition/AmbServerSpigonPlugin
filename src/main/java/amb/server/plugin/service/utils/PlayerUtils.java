package amb.server.plugin.service.utils;

import org.bukkit.entity.Player;

/**
 * @author zhangrenjing
 * created on 2021/3/7
 */
public class PlayerUtils {
    // 雷达使用后冷却的tag
    public static final String PLAYER_RADAR_COOLING = "Amb_Radar_Cooling";
    // 建筑蓝图选择器 使用后冷却的tag
    public static final String PLAYER_BLUEPRINT_SELECT = "Amb_Blueprint_Select_Cooling";

    public static boolean mark(Player player, String key) {
        return player.removeScoreboardTag(key);
    }

    public static boolean hasMark(Player player, String key) {
        return player.getScoreboardTags().contains(key);
    }

    public static boolean notMark(Player player, String key) {
        return !player.getScoreboardTags().contains(key);
    }

    public static boolean unMark(Player player, String key) {
        return player.removeScoreboardTag(key);
    }

    /**
     * 通过 等级 获取玩家经验，没有考虑当前经验
     * @param player
     * @return
     */
    public static double getExp(Player player) {
        int level = player.getLevel();
        return getExp(level);
    }
    public static double getExp(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return Math.pow(level, 2) + 6 * level;
        } else if (level <= 31) {
            return 2.5 * Math.pow(level, 2) - 40.5 * level + 360;
        } else {
            return 4.5 * Math.pow(level, 2) - 162.5 * level + 2220;
        }
    }
}
