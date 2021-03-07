package amb.server.plugin.service.utils;

import org.bukkit.entity.Player;

/**
 * @author zhangrenjing
 * created on 2021/3/7
 */
public class PlayerUtils {
    // �״�ʹ�ú���ȴ��tag
    public static final String PLAYER_RADAR_COOLING = "Amb_Radar_Cooling";
    // ������ͼѡ���� ʹ�ú���ȴ��tag
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

    public static double getExp(Player player) {
        int level = player.getLevel();
        if (level <= 16) {
            return Math.pow(level, 2) + 6 * level;
        } else if (level <= 31) {
            return 2.5 * Math.pow(level, 2) - 40.5 * level + 360;
        } else {
            return 4.5 * Math.pow(level, 2) - 162.5 * level + 2220;
        }
    }

    public static void changeExp(Player player, double exp) {
        double currentExp = getExp(player);
        player.setExp(0);
        player.setLevel(0);
        player.giveExp((int) (currentExp + exp));
    }
}
