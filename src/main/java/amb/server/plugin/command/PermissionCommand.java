package amb.server.plugin.command;

import amb.server.plugin.service.permission.PermissionService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PermissionCommand implements CommandExecutor {

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player){
            Player player = (Player) commandSender;
            // amb permission <-u|-p> <add|del|get> [player] [permission]
            if ("amb".equals(s) && strings.length > 0){
                if (strings[0].equals("permission") && strings.length > 4){
                    UUID uuid = null;
                    if (strings[1].equals("-u")){
                        try {
                            uuid = UUID.fromString(strings[3]);
                        } catch (Exception e){
                            player.sendMessage("UUID"+strings[3]+"错误");
                            return false;
                        }
                    } else {
                        Player tPlayer = Bukkit.getServer().getPlayer(strings[3]);
                        if (tPlayer == null){
                            player.sendMessage("玩家"+strings[3]+"不在线,请使用-u进行uuid操作");
                            return false;
                        }
                        uuid = tPlayer.getUniqueId();
                    }

                    if (strings[2].equals("add")){
                        PermissionService.addPermission(uuid, strings[4]);
                        player.sendMessage("添加权限成功!"+uuid+"+"+strings[4]);
                    } else if (strings[2].equals("del")){
                        PermissionService.delPermission(uuid, strings[4]);
                        player.sendMessage("删除权限成功!"+uuid+"-"+strings[4]);
                    } else if (strings[2].equals("get")){
                        List<String> pps =PermissionService.getPermission(uuid);
                        player.sendMessage((pps == null || pps.isEmpty())? "结果为空" : pps.toString());
                    }
                    return true;
                }
            }
            /* 请求传送 应答代码 相关命令已删除
            else if ("tpbrequest".equals(s) && strings.length == 2){
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
            }*/
        }
        return false;
    }
}
