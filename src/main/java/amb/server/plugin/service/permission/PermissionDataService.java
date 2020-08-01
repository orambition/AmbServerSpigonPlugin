package amb.server.plugin.service.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static amb.server.plugin.config.PluginConfig.ambPermissionSaveData;
import static amb.server.plugin.config.PluginConfig.savePermissionDate;

public class PermissionDataService {

    public static void addPermission(UUID uuid, String premission){
        List<String> pps = getPermission(uuid);
        if (pps == null){
            pps = new ArrayList<>();
        }
        pps.add(premission);
        ambPermissionSaveData.set("player."+uuid, pps);
        savePermissionDate();
    }

    public static List<String> getPermission(UUID uuid){
        return (List<String>) ambPermissionSaveData.getList("player."+uuid.toString());
    }

    public static void delPermission(UUID uuid, String premission){
        List<String> pps = getPermission(uuid);
        if (pps == null || !pps.contains(premission)){
            return;
        }
        pps.remove(premission);
        ambPermissionSaveData.set("player."+uuid, pps);
        savePermissionDate();
    }
}
