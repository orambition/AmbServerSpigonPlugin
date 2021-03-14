package amb.server.plugin.service.utils;

import amb.server.plugin.core.PluginCore;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * @author zhangrenjing
 * created on 2021/3/7
 */
public class PlayerUtils {
    // 雷达使用后冷却的tag
    public static final String PLAYER_RADAR_COOLING = "Amb_Radar_Cooling";
    // 建筑蓝图选择器 使用后冷却的tag
    public static final String PLAYER_BLUEPRINT_SELECT = "Amb_Blueprint_Select_Cooling";

    public static final String PLAYER_DM_KEY_SELECT_LOCATION_1 = "selected pos 1";
    public static final String PLAYER_DM_KEY_SELECT_LOCATION_2 = "selected pos 2";
    public static final String PLAYER_DM_KEY_SELECT_LOCATION_3 = "selected pos 3";

    public static boolean mark(Player player, String key) {
        return player.removeScoreboardTag(key);
    }
    public static boolean unMark(Player player, String key) {
        return player.removeScoreboardTag(key);
    }

    public static boolean hasMark(Player player, String key) {
        return player.getScoreboardTags().contains(key);
    }

    public static boolean notMark(Player player, String key) {
        return !player.getScoreboardTags().contains(key);
    }

    public static void setMetadata(Player player, String key, Object obj) {
        player.setMetadata(key, new FixedMetadataValue(PluginCore.getInstance(), obj));
    }
    public static Object getMetadata(Player player, String key) {
        if (player.hasMetadata(key) && player.getMetadata(key) != null && !player.getMetadata(key).isEmpty()) {
            return player.getMetadata(key).get(0).value();
        }
        return null;
    }
    public static void removeMetadata(Player player, String key) {
        player.removeMetadata(key, PluginCore.getInstance());
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
