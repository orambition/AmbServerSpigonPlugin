package amb.server.plugin.service.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GUIUtils {
    /**
     * 生成能量条形式的文本
     * [██████]
     * @param num
     * @param max
     * @return
     */
    public static String buildBatter(int num, int max){
        if (num > max){
            num = max;
        }
        int haveCount = num;
        int emptyCount = max-num;
        StringBuilder result = new StringBuilder(ChatColor.GREEN + "[");
        for (int i=0; i < haveCount; i++){
            result.append('▊');
        }
        result.append(ChatColor.DARK_BLUE);
        for (int i=0; i < emptyCount; i++){
            result.append('▊');
        }
        result.append(ChatColor.GREEN + "]");
        return result.toString();
    }

    public static void sendMsg(Player player, String msg){
        player.sendTitle(" ",ChatColor.GREEN + msg,10,30,10);
    }
}
