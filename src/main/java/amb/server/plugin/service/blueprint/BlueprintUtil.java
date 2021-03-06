package amb.server.plugin.service.blueprint;

import amb.server.plugin.core.PluginCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import static amb.server.plugin.service.blueprint.BlueprintService.SELECT_LOCATION_1;
import static amb.server.plugin.service.blueprint.BlueprintService.SELECT_LOCATION_2;

/**
 * @author zhangrenjing
 * created on 2021/3/6
 */
public class BlueprintUtil {

    public static Location setSelectedLocation(Player player, String pos, Location location) {
        player.removeMetadata(pos, PluginCore.getInstance());
        player.setMetadata(pos, new FixedMetadataValue(PluginCore.getInstance(), location));
        player.sendMessage("[½¨ÖþÀ¶Í¼]" + pos
                + ". "+ location.getWorld().getName()
                + ": x=" + location.getBlockX()
                + ", y=" + location.getBlockY()
                + ", z=" + location.getBlockZ());
        return location;
    }

    public static Location getSelectedLocation(Player player, String pos) {
        if (player.hasMetadata(pos) && player.getMetadata(pos) != null && player.getMetadata(pos).size() > 0) {
            return ((Location) player.getMetadata(pos).get(0).value());
        }
        return null;
    }

    public static void delSelected(Player player) {
        player.removeMetadata(SELECT_LOCATION_1, PluginCore.getInstance());
        player.removeMetadata(SELECT_LOCATION_2, PluginCore.getInstance());
    }
}
