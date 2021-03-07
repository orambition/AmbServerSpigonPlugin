package amb.server.plugin.service.blueprint;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.utils.GUIUtils;
import amb.server.plugin.service.utils.ParticleUtils;
import amb.server.plugin.service.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

import static amb.server.plugin.service.blueprint.BlueprintUtil.*;
import static amb.server.plugin.service.utils.PlayerUtils.PLAYER_BLUEPRINT_SELECT;

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
        if (player.hasPermission(PermissionConstant.BLUEPRINT)
                && PlayerUtils.notMark(player, PLAYER_BLUEPRINT_SELECT)) {
            event.setCancelled(true);
            Action action = event.getAction();
            // (�һ��� && ���岻�ǿɽ�����) || �Ҽ�����
            if (action.equals(Action.LEFT_CLICK_BLOCK)) {
                Location location1 = BlueprintUtil.setSelectedLocation(player, SELECT_LOCATION_1, event.getClickedBlock().getLocation());
                Location location2 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_2);
                if (location2 != null) {
                    // ǰ��(Shift+) Ϊ�ƻ�����
                    preprocessed(player, location1, location2, player.isSneaking());
                }
            } else if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                Location location2 = BlueprintUtil.setSelectedLocation(player, SELECT_LOCATION_2, event.getClickedBlock().getLocation());
                Location location1 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_1);
                if (location1 != null) {
                    preprocessed(player, location1, location2, player.isSneaking());
                }
            }
        }
    }

    /**
     * ���Ԥ��������У�飬�򿪲���/���������棬
     * ����ҳ��ر��¼�����ʼ���/�ƻ�
     *
     * @param player
     * @param pos1
     * @param pos2
     * @param isBreak �Ƿ�Ϊ�ƻ�����
     */
    private static void preprocessed(Player player, Location pos1, Location pos2, boolean isBreak) {
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
        // �Ӵ�������ͬʱֻ��ִ��һ������
        PlayerUtils.mark(player, PLAYER_BLUEPRINT_SELECT);
        // ��������
        ParticleUtils.drawLineTimer(pos1, pos2);

        if (isBreak) {
            // �ƻ�����������ǵ�ǰ�߳���ʱִ�У�����������"�첽"�ģ������޷����첽�߳��д�
            Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
                Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.blueprintBreakItemPutName);
                player.openInventory(inventory);
            }, 40);
            GUIUtils.sendMsg(player, "����ѡ��ɹ�������빤��");
        } else {
            // ����
            Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
                Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.blueprintBuildItemPutName);
                player.openInventory(inventory);
            }, 40);
            GUIUtils.sendMsg(player, "����ѡ��ɹ�������뽨�����");
        }
    }

    public static void doBlueprint(Player player, ItemStack[] itemContents, boolean isBreak) {
        PlayerUtils.unMark(player, PLAYER_BLUEPRINT_SELECT);
        if (itemContents == null || itemContents.length == 0) {
            return;
        }
        List<ItemStack> validItem = new ArrayList<>();
        for (ItemStack itemStack : itemContents) {
            if (itemStack == null) continue;
            if (isBreak ? BlueprintUtil.isValueBreakItem(itemStack) : BlueprintUtil.isValueBuildItem(itemStack)) {
                validItem.add(itemStack);
            } else {
                player.getWorld().dropItem(player.getLocation(), itemStack);
            }
        }
        if (validItem.isEmpty()) return;

        Location pos1 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_1);
        Location pos2 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_2);
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || pos1.getWorld() != pos2.getWorld()) {
            player.sendMessage("[������ͼ]ѡ��λ����Ч��");
            return;
        }
        BlueprintUtil.delSelected(player);
        int[] xyzRange = BlueprintUtil.getRange(pos1, pos2);
        World world = pos1.getWorld();

        if (isBreak) {
            // �ƻ�
            asyncBreak(player, validItem, world, xyzRange);
        } else {
            // ���
            asyncBuild(player, validItem, world, xyzRange);
        }

    }

    private static void asyncBreak(Player player, List<ItemStack> validItem, World world, int[] xyzRange) {
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            Map<String, List<ItemStack>> validItemLaitMap = BlueprintUtil.convertItemList2Map(validItem);

            for (int x = xyzRange[0]; x <= xyzRange[1]; x++) {
                for (int y = xyzRange[2]; y <= xyzRange[3]; y++) {
                    for (int z = xyzRange[4]; z <= xyzRange[5]; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.isEmpty() || block.isPassable() || block.getType() == Material.BEDROCK) {
                            continue;
                        }
                        if (PlayerUtils.getExp(player) < PluginConfig.blueprintBreakItemNeedExpCount) {
                            player.sendMessage("[������ͼ]û�о��飬����ʹ����ͼ�ھ�");
                            backItem2Player(player, validItem);
                            return;
                        }
                        ItemStack index = null;
                        if (block.getType() == Material.GRASS_BLOCK
                                || block.getType() == Material.DIRT
                                || block.getType() == Material.COARSE_DIRT
                                || block.getType() == Material.PODZOL
                                || block.getType() == Material.SAND
                                || block.getType() == Material.GRAVEL) {
                            // ��
                            if (validItemLaitMap.containsKey(_SHOVEL)) {
                                Optional<ItemStack> shovel = validItemLaitMap.get(_SHOVEL).stream().filter(i -> ((Damageable) i.getItemMeta()).getDamage() > 0).findFirst();
                                if (shovel.isPresent()) {
                                    index = shovel.get();
                                }
                            }
                        } else if (block.getType().name().contains("_WOOD")
                                || block.getType().name().contains("_LOG")
                                || block.getType().name().contains("_PLANKS")) {
                            // ��
                            if (validItemLaitMap.containsKey(_AXE)) {
                                Optional<ItemStack> shovel = validItemLaitMap.get(_AXE).stream().filter(i -> ((Damageable) i.getItemMeta()).getDamage() > 0).findFirst();
                                if (shovel.isPresent()) {
                                    index = shovel.get();
                                }
                            }
                        } else {
                            // ��
                            if (validItemLaitMap.containsKey(_PICKAXE)) {
                                validItemLaitMap.get(_PICKAXE).stream().forEach(i -> player.sendMessage("�;�=" + ((Damageable) i.getItemMeta()).getDamage()));
                                Optional<ItemStack> shovel = validItemLaitMap.get(_PICKAXE).stream().filter(i -> ((Damageable) i.getItemMeta()).getDamage() > 0).findFirst();
                                if (shovel.isPresent()) {
                                    index = shovel.get();
                                } else {
                                    player.sendMessage("û�и�ͷ");
                                }
                            }
                        }
                        if (index != null) {
                            ItemStack finalIndex = index;
                            Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
                                if (block.breakNaturally(finalIndex)) {
                                    ((Damageable) finalIndex.getItemMeta()).setDamage(((Damageable) finalIndex.getItemMeta()).getDamage() - 1);
                                    player.giveExp(-PluginConfig.blueprintBreakItemNeedExpCount);
                                } else {
                                    player.sendMessage("[������ͼ]û�ڶ�");
                                }
                            }, (x + y + z) * 20);

                        } else {
                            player.sendMessage("[������ͼ]�ھ�" + block.getType().name() + "ȱ�ٱ�Ҫ�Ĺ���");
                        }
                    }
                }
            }
            backItem2Player(player, validItem);
        });
    }

    private static void asyncBuild(Player player, List<ItemStack> validItem, World world, int[] xyzRange) {
        // �����첽������
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            Iterator<ItemStack> itemStackIterator = validItem.iterator();
            ItemStack index = itemStackIterator.next();
            for (int x = xyzRange[0]; x <= xyzRange[1]; x++) {
                for (int y = xyzRange[2]; y <= xyzRange[3]; y++) {
                    for (int z = xyzRange[4]; z <= xyzRange[5]; z++) {
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
            backItem2Player(player, validItem);
        });
    }

    private static void backItem2Player(Player player, List<ItemStack> validItem) {
        validItem.stream().filter(i -> ((Damageable) i.getItemMeta()).getDamage() > 0)
                .forEach(i -> {
                    player.getWorld().dropItem(player.getLocation(), i);
                    player.sendMessage("[������ͼ] ��Ʒ�黹��" + i.getType().name() + "x" + i.getAmount());
                });
    }
}
