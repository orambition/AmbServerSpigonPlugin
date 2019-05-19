package amb.server.plugin.command;

import amb.server.plugin.service.tpb.TpBookService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static amb.server.plugin.service.tpb.TpBookDataService.addPublicTeleporter;

public class TpBookCommand implements CommandExecutor {

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player){
            if ("tpb".equals(s) && strings.length > 0){
                if (strings[0].equals("add") && strings.length > 1){
                    addPublicTeleporter((Player) commandSender,strings[1]);
                    return true;
                }
            }else if ("tpbrequest".equals(s) && strings.length == 2){
                Player player = (Player)commandSender;
                // string[1] 请求传送的玩家姓名
                if (player.getScoreboardTags().contains("reqTp-"+strings[1])){
                    player.removeScoreboardTag("reqTp-"+strings[1]);
                    Player reqPlayer = Bukkit.getPlayer(strings[1]);
                    if (reqPlayer != null && reqPlayer.isOnline()){
                        if ("agree".equals(strings[0])){
                            TpBookService.tpPlayerToPlayer(reqPlayer, player);
                        }else if ("deny".equals(strings[0])){
                            reqPlayer.sendMessage(ChatColor.RED + player.getDisplayName()+"拒绝了您的传送请求");
                            player.sendMessage("已拒绝"+strings[1] +"的传送请求");
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}
