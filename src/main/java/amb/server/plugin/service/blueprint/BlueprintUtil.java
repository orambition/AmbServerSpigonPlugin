package amb.server.plugin.service.blueprint;

import amb.server.plugin.core.PluginCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static amb.server.plugin.service.blueprint.BlueprintService.SELECT_LOCATION_1;
import static amb.server.plugin.service.blueprint.BlueprintService.SELECT_LOCATION_2;

/**
 * @author zhangrenjing
 * created on 2021/3/6
 */
public class BlueprintUtil {
    // ½££º_SWORD¡¢ÇÂ£º_SHOVEL¡¢¸ä£º_PICKAXE¡¢¸«£º_AXE¡¢³ú£º_HOE
    public static final String _SWORD = "_SWORD";
    public static final String _SHOVEL = "_SHOVEL";
    public static final String _PICKAXE = "_PICKAXE";
    public static final String _AXE = "_AXE";
    public static final String _HOE = "_HOE";
    private static final String[] validBreakItem = new String[]{_SHOVEL, _PICKAXE, _AXE};

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

    public static int[] getRange(Location pos1, Location pos2) {
        int p1x = pos1.getBlockX();
        int p1y = pos1.getBlockY();
        int p1z = pos1.getBlockZ();

        int p2x = pos2.getBlockX();
        int p2y = pos2.getBlockY();
        int p2z = pos2.getBlockZ();

        int xMin, yMin, zMin, xMax, yMax, zMax;
        if (p1x < p2x) {
            xMin = p1x;
            xMax = p2x;
        } else {
            xMin = p2x;
            xMax = p1x;
        }
        if (p1y < p2y) {
            yMin = p1y;
            yMax = p2y;
        } else {
            yMin = p2y;
            yMax = p1y;
        }
        if (p1z < p2z) {
            zMin = p1z;
            zMax = p2z;
        } else {
            zMin = p2z;
            zMax = p1z;
        }
        return new int[]{xMin, xMax, yMin, yMax, zMin, zMax};
    }

    public static boolean isValueBuildItem(ItemStack itemStack) {
        return itemStack != null
                && itemStack.getType().isBlock()
                && itemStack.getType().isSolid()
                && itemStack.getAmount() > 0
                && !itemStack.getType().name().contains("_DOOR");
    }

    public static boolean isValueBreakItem(ItemStack currentItem) {
        // ½££º_SWORD¡¢ÇÂ£º_SHOVEL¡¢¸ä£º_PICKAXE¡¢¸«£º_AXE¡¢³ú£º_HOE
        return currentItem != null
                && (currentItem.getType().name().contains(_SHOVEL)
                || currentItem.getType().name().contains(_PICKAXE)
                || currentItem.getType().name().contains(_AXE));
    }

    public static Map<String, List<ItemStack>> convertItemList2Map(Player player, List<ItemStack> itemStackList) {
        if (itemStackList == null || itemStackList.isEmpty()) {
            return null;
        }
        Map<String, List<ItemStack>> stringListMap = new HashMap<>();
        for (ItemStack i : itemStackList) {
            for (String s : validBreakItem) {
                if (i.getType().name().contains(s)) {
                    Damageable damageable = ((Damageable) i.getItemMeta());
                    player.sendMessage("demage=" + damageable.getDamage() + "has=" + damageable.hasDamage());

                    if (!damageable.hasDamage()) {
                        damageable.setDamage(i.getType().getMaxDurability());
                        i.setItemMeta((ItemMeta) damageable);
                    }
                    stringListMap.putIfAbsent(s, new ArrayList<>());
                    stringListMap.get(s).add(i);
                    break;
                }
            }
        }
        return stringListMap;
    }

}
