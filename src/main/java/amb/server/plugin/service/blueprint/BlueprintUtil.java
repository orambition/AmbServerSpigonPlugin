package amb.server.plugin.service.blueprint;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

import static amb.server.plugin.service.utils.PlayerUtils.*;

/**
 * @author zhangrenjing
 * created on 2021/3/6
 */
public class BlueprintUtil {

    // 剑：_SWORD、锹：_SHOVEL、镐：_PICKAXE、斧：_AXE、锄：_HOE
    public static final String _SWORD = "_SWORD";
    public static final String _SHOVEL = "_SHOVEL";
    public static final String _PICKAXE = "_PICKAXE";
    public static final String _AXE = "_AXE";
    public static final String _HOE = "_HOE";
    private static final String[] validBreakItem = new String[]{_SHOVEL, _PICKAXE, _AXE};

    public static Location setSelectedLocation1(Player player, Location location) {
        return setSelectedLocation(player, PLAYER_DM_KEY_SELECT_LOCATION_1, location);
    }
    public static Location setSelectedLocation2(Player player, Location location) {
        return setSelectedLocation(player, PLAYER_DM_KEY_SELECT_LOCATION_2, location);
    }
    public static Location setSelectedLocation3(Player player, Location location) {
        return setSelectedLocation(player, PLAYER_DM_KEY_SELECT_LOCATION_3, location);
    }

    private static Location setSelectedLocation(Player player, String pos, Location location) {
        PlayerUtils.setMetadata(player, pos, location);
        player.playSound(location, pos.equals(PLAYER_DM_KEY_SELECT_LOCATION_1) ? Sound.BLOCK_NOTE_BLOCK_BASS : Sound.BLOCK_NOTE_BLOCK_BANJO, 2, 1);
        player.sendMessage("[建筑蓝图] " + pos);
        return location;
    }

    public static boolean isValidRange(Player player, Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || !Objects.equals(pos1.getWorld(), pos2.getWorld())) {
            return false;
        }

        int xSize = Math.abs(pos1.getBlockX() - pos2.getBlockX());
        int ySize = Math.abs(pos1.getBlockY() - pos2.getBlockY());
        int zSize = Math.abs(pos1.getBlockZ() - pos2.getBlockZ());
        if (xSize > PluginConfig.blueprintSelectorMaxRange
                || ySize > PluginConfig.blueprintSelectorMaxRange
                || zSize > PluginConfig.blueprintSelectorMaxRange) {
            player.sendMessage("[建筑蓝图] 选择范围过大，请重新选择！");
            return false;
        }
        return true;
    }

    public static Location getSelectedLocation1(Player player) {
        return getSelectedLocation(player, PLAYER_DM_KEY_SELECT_LOCATION_1);
    }
    public static Location getSelectedLocation2(Player player) {
        return getSelectedLocation(player, PLAYER_DM_KEY_SELECT_LOCATION_2);
    }
    public static Location getSelectedLocation3(Player player) {
        return getSelectedLocation(player, PLAYER_DM_KEY_SELECT_LOCATION_3);
    }

    private static Location getSelectedLocation(Player player, String pos) {
        Object obj = PlayerUtils.getMetadata(player, pos);
        if (obj != null) {
            return (Location) obj;
        }
        return null;
    }

    public static void delSelected(Player player) {
        PlayerUtils.removeMetadata(player, PLAYER_DM_KEY_SELECT_LOCATION_1);
        PlayerUtils.removeMetadata(player, PLAYER_DM_KEY_SELECT_LOCATION_2);
        PlayerUtils.removeMetadata(player, PLAYER_DM_KEY_SELECT_LOCATION_3);
    }
    public static void delSelected1(Player player) {
        PlayerUtils.removeMetadata(player, PLAYER_DM_KEY_SELECT_LOCATION_1);
    }
    public static void delSelected2(Player player) {
        PlayerUtils.removeMetadata(player, PLAYER_DM_KEY_SELECT_LOCATION_2);
    }
    public static void delSelected3(Player player) {
        PlayerUtils.removeMetadata(player, PLAYER_DM_KEY_SELECT_LOCATION_3);
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

    /**
     * 材料填充界面 校验材料是否有效
     *
     * @param itemStack
     * @return
     */
    public static boolean isValueBuildItem(ItemStack itemStack) {
        return itemStack != null && isValueMaterial(itemStack.getType());
    }

    public static boolean isValueCopyBlock(Block block) {
        return block != null && isValueMaterial(block.getType());
    }
    public static boolean isValueMaterial(Material material) {
        return material != null
                && material.isBlock()
                && material.isSolid()
                && !material.name().contains("_DOOR");
    }

    public static void syncBuild(Map<Block, Material> needProcessBlockMap) {
        if (needProcessBlockMap == null || needProcessBlockMap.isEmpty()) return;
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            for (Map.Entry<Block, Material> entry : needProcessBlockMap.entrySet()) {
                entry.getKey().setType(entry.getValue());
                //entry.getKey().getWorld().spawnFallingBlock(entry.getKey().getLocation().add(0.5, 20, 0.5), entry.getValue().createBlockData());
            }
        });
    }

    public static void syncBackBuildItem(Player player, List<ItemStack> backList) {
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            StringBuilder stringBuilder = new StringBuilder("[建筑蓝图] 材料归还：");
            backList.forEach(i -> {
                player.getWorld().dropItem(player.getLocation(), i);
                stringBuilder.append(i.getType().name()).append("x").append(i.getAmount()).append(", ");
            });
            player.sendMessage(stringBuilder.toString());
        });
    }
}
