package amb.server.plugin.service.tools;

import org.bukkit.ChatColor;

public class GUITools {
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
}
