package amb.server.plugin.service.blueprint;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.tpb.TpBookGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangrenjing
 * created on 2021/3/6
 */
public class BlueprintService {
    public static final String SELECT_LOCATION_1 = "selected pos 1";
    public static final String SELECT_LOCATION_2 = "selected pos 2";

    /**
     * ʹ�� ��ͼѡ����
     *
     * @param event
     */
    public static void useSelectorEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PermissionConstant.BLUEPRINT)) {
            player.sendMessage("[������ͼ]��Ȩ��ʹ��!����ϵAmb");
            return;
        }
        // event.setCancelled(true);
        // ǰ��(Shift+)
        if (player.isSneaking()) {

        } else {
            Action action = event.getAction();
            // (�һ��� && ���岻�ǿɽ�����) || �Ҽ�����
            if (action.equals(Action.LEFT_CLICK_BLOCK)) {
                Location location1 = BlueprintUtil.setSelectedLocation(player, SELECT_LOCATION_1, event.getClickedBlock().getLocation());
                Location location2 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_2);
                if (location2 != null) {
                    preprocessed(player, location1, location2);
                }
            } else if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                Location location2 = BlueprintUtil.setSelectedLocation(player, SELECT_LOCATION_2, event.getClickedBlock().getLocation());
                Location location1 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_1);
                if (location1 != null) {
                    preprocessed(player, location1, location2);
                }
            }
        }
    }

    /**
     * ���Ԥ��������У�飬�򿪲��������棬
     * ����ҳ��ر��¼�����ʼ���
     *
     * @param player
     * @param pos1
     * @param pos2
     */
    private static void preprocessed(Player player, Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || !Objects.equals(pos1.getWorld(), pos2.getWorld())) {
            return;
        }

        int xSize = Math.abs(pos1.getBlockX() - pos2.getBlockX());
        int ySize = Math.abs(pos1.getBlockY() - pos2.getBlockY());
        int zSize = Math.abs(pos1.getBlockZ() - pos2.getBlockZ());
        if (xSize > PluginConfig.blueprintSelectorMaxRange
                || ySize > PluginConfig.blueprintSelectorMaxRange
                || zSize > PluginConfig.blueprintSelectorMaxRange) {
            player.sendMessage("[������ͼ]ѡ��Χ����������ѡ��");
            return;
        }
        Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.blueprintBuildItemPutName);
        player.openInventory(inventory);
    }

    public static void doPut(Player player, ItemStack[] materialsContents) {
        if (materialsContents == null || materialsContents.length == 0) {
            return;
        }
        List<ItemStack> validItem = new ArrayList<>();
        for (ItemStack itemStack : materialsContents) {
            if (itemStack == null) continue;
            if (itemStack.getType().isBlock() && itemStack.getType().isSolid() && itemStack.getAmount() > 0) {
                validItem.add(itemStack);
            } else {
                player.getWorld().dropItem(player.getLocation(), itemStack);
            }
        }
        if (validItem.size() == 0) {
            return;
        }

        Location pos1 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_1);
        Location pos2 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_2);
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || pos1.getWorld() != pos2.getWorld()) {
            player.sendMessage("[������ͼ]ѡ��λ����Ч��");
            return;
        }
        BlueprintUtil.delSelected(player);

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

        World world = pos1.getWorld();
        Iterator<ItemStack> itemStackIterator = validItem.iterator();
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            ItemStack index = itemStackIterator.next();
            for (int x = xMin; x <= xMax; x++) {
                for (int y = yMin; y <= yMax; y++) {
                    for (int z = zMin; z <= zMax; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.isEmpty() || block.isPassable()) {
                            while (index.getAmount() <= 0 && itemStackIterator.hasNext()) {
                                index = itemStackIterator.next();
                            }
                            if (index.getAmount() > 0) {
                                block.setType(index.getType());
                                index.setAmount(index.getAmount() - 1);
                            } else {
                                player.sendMessage("[������ͼ]���ϲ��㣡������ǰ����");
                                return;
                            }
                        }
                    }
                }
            }
            validItem.stream().filter(i -> i.getAmount() > 0)
                    .forEach(i -> {
                        player.getWorld().dropItem(player.getLocation(), i);
                        player.sendMessage("[������ͼ]����ʣ�ࣺ" + i.getItemMeta().getDisplayName() + "x" + i.getAmount());
                    });
        });
    }
}
