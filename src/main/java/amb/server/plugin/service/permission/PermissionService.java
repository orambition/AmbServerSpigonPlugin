package amb.server.plugin.service.permission;

import amb.server.plugin.core.PluginCore;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PermissionService {

    public static void setupPermission(Player player){
        if (player == null){
            return;
        }
        PermissionAttachment attachment = player.addAttachment(PluginCore.getInstance());
        PluginCore.getPermissionMap().put(player.getUniqueId(), attachment);

        List<String> pps = PermissionDataService.getPermission(player.getUniqueId());
        if (pps != null && pps.size() > 0) {
            pps.forEach(p->attachment.setPermission(p,true));
        }
    }

    public static void clearPermission(Player player){
        if (player == null){
            return;
        }
        if (PluginCore.getPermissionMap().containsKey(player.getUniqueId())){
            player.removeAttachment(PluginCore.getPermissionMap().get(player.getUniqueId()));
            PluginCore.getPermissionMap().remove(player.getUniqueId());
        }
    }

    public static void addPermission(UUID uuid, String permission){
        if (uuid == null || StringUtils.isEmpty(permission)){
            return;
        }
        PluginCore.getPermissionMap().get(uuid).setPermission(permission, true);
        PermissionDataService.addPermission(uuid, permission);
    }

    public static void delPermission(UUID uuid, String permission){
        if (uuid == null || StringUtils.isEmpty(permission) || !PluginCore.getPermissionMap().containsKey(uuid)){
            return;
        }
        PluginCore.getPermissionMap().get(uuid).unsetPermission(permission);
        PermissionDataService.delPermission(uuid, permission);
    }

    public static List<String> getPermission(UUID uuid){
        if (uuid == null || !PluginCore.getPermissionMap().containsKey(uuid)){
            return null;
        }
        return PermissionDataService.getPermission(uuid);
    }
}
