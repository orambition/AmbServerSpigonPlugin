package amb.server.plugin.command;

import amb.server.plugin.service.permission.PermissionService;
import amb.server.plugin.service.tpb.TpBookService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static amb.server.plugin.service.tpb.TpBookDataService.addPublicTeleporter;

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
                            player.sendMessage("UUID"+strings[3]+"����");
                        }
                    } else {
                        Player tPlayer = Bukkit.getServer().getPlayer(strings[3]);
                        if (tPlayer == null){
                            player.sendMessage("���"+strings[3]+"������,��ʹ��-u����uuid����");
                            return false;
                        }
                        uuid = tPlayer.getUniqueId();
                    }

                    if (strings[2].equals("add")){
                        PermissionService.addPermission(uuid, strings[4]);
                        player.sendMessage("����Ȩ�޳ɹ�!"+uuid+"+"+strings[4]);
                    } else if (strings[2].equals("del")){
                        PermissionService.delPermission(uuid, strings[4]);
                        player.sendMessage("ɾ��Ȩ�޳ɹ�!"+uuid+"-"+strings[4]);
                    } else if (strings[2].equals("get")){
                        List<String> pps =PermissionService.getPermission(uuid);
                        player.sendMessage((pps == null || pps.size()==0)? "���Ϊ��" : pps.toString());
                    }
                    return true;
                }
            }
            /* ������ Ӧ����� ���������ɾ��
            else if ("tpbrequest".equals(s) && strings.length == 2){
                Player player = (Player)commandSender;
                // string[1] �����͵��������
                if (player.getScoreboardTags().contains("reqTp-"+strings[1])){
                    player.removeScoreboardTag("reqTp-"+strings[1]);
                    Player reqPlayer = Bukkit.getPlayer(strings[1]);
                    if (reqPlayer != null && reqPlayer.isOnline()){
                        if ("agree".equals(strings[0])){
                            TpBookService.tpPlayerToPlayer(reqPlayer, player);
                        }else if ("deny".equals(strings[0])){
                            reqPlayer.sendMessage(ChatColor.RED + player.getDisplayName()+"�ܾ������Ĵ�������");
                            player.sendMessage("�Ѿܾ�"+strings[1] +"�Ĵ�������");
                        }
                    }
                }
                return true;
            }*/
        }
        return false;
    }
}