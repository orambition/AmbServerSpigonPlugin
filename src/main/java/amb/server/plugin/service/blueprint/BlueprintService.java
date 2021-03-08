package amb.server.plugin.service.blueprint;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.utils.GUIUtils;
import amb.server.plugin.service.utils.ItemUtils;
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

import java.util.*;
import java.util.stream.Collectors;

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
            Action action = event.getAction();
            if (action.equals(Action.LEFT_CLICK_BLOCK)) {
                event.setCancelled(true);
                Location location1 = BlueprintUtil.setSelectedLocation(player, SELECT_LOCATION_1, event.getClickedBlock().getLocation());
                Location location2 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_2);
                if (location2 != null) {
                    if (location1.equals(location2)) {
                        BlueprintUtil.delSelected(player, SELECT_LOCATION_2);
                    } else {
                        // ǰ��(Shift+) Ϊ�ƻ�����
                        preprocessed(player, location1, location2, player.isSneaking());
                    }
                }
            } else if (action.equals(Action.RIGHT_CLICK_BLOCK) && !event.getClickedBlock().getType().isInteractable()) {
                event.setCancelled(true);
                Location location2 = BlueprintUtil.setSelectedLocation(player, SELECT_LOCATION_2, event.getClickedBlock().getLocation());
                Location location1 = BlueprintUtil.getSelectedLocation(player, SELECT_LOCATION_1);
                if (location1 != null) {
                    if (location1.equals(location2)) {
                        BlueprintUtil.delSelected(player, SELECT_LOCATION_1);
                    } else {
                        preprocessed(player, location1, location2, player.isSneaking());
                    }
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
            player.sendMessage("[������ͼ] ѡ��Χ����������ѡ��");
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
            }, 30);
            GUIUtils.sendMsg(player, "����ѡ��ɹ�������빤��");
            player.sendMessage("[������ͼ] ����ѡ��ɹ��������[����]. (�ھ����ľ���ֵ)");
        } else {
            // ����
            Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
                Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.blueprintBuildItemPutName);
                player.openInventory(inventory);
            }, 30);
            GUIUtils.sendMsg(player, "����ѡ��ɹ�������뽨�����");
            player.sendMessage("[������ͼ] ����ѡ��ɹ�������뽨��[����]");
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
            player.sendMessage("[������ͼ] ѡ��λ����Ч��");
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

    /**
     * �첽�ƻ�
     *
     * @param player
     * @param validItem
     * @param world
     * @param xyzRange
     */
    private static void asyncBreak(Player player, List<ItemStack> validItem, World world, int[] xyzRange) {
        // �첽���У�����Ҫ�ύ�������߳̽���ͬ��ִ��
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PluginCore.getInstance(), () -> {
            Map<String, List<ItemStack>> validItemLaitMap = BlueprintUtil.convertItemList2Map(validItem);
            Map<Block, ItemStack> needProcessBlockMap = new HashMap<>();
            Set<Material> notBreakSet = new HashSet<>();
            for (int x = xyzRange[0]; x <= xyzRange[1]; x++) {
                for (int y = xyzRange[2]; y <= xyzRange[3]; y++) {
                    for (int z = xyzRange[4]; z <= xyzRange[5]; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.isEmpty() || block.isPassable() || block.getType() == Material.BEDROCK) {
                            continue;
                        }
                        if (PlayerUtils.getExp(player) < PluginConfig.blueprintBreakItemNeedExpCount) {
                            player.sendMessage("[������ͼ] ���鲻�㣬�޷�ʹ����ͼ�ھ�");
                            backItem2Player(player, validItem);
                            breakProcess(needProcessBlockMap);
                            return;
                        }
                        ItemStack index = null;
                        if (block.getType() == Material.GRASS_BLOCK || block.getType() == Material.DIRT
                                || block.getType() == Material.COARSE_DIRT || block.getType() == Material.PODZOL
                                || block.getType() == Material.SAND || block.getType() == Material.GRAVEL) {
                            // ��
                            if (validItemLaitMap.containsKey(_SHOVEL)) {
                                Optional<ItemStack> shovel = validItemLaitMap.get(_SHOVEL).stream().filter(i -> ItemUtils.hasDurability(i, 1)).findFirst();
                                if (shovel.isPresent()) {
                                    index = shovel.get();
                                }
                            }
                        } else if (block.getType().name().contains("_WOOD")
                                || block.getType().name().contains("_LOG")
                                || block.getType().name().contains("_PLANKS")) {
                            // ��
                            if (validItemLaitMap.containsKey(_AXE)) {
                                Optional<ItemStack> shovel = validItemLaitMap.get(_AXE).stream().filter(i -> ItemUtils.hasDurability(i, 1)).findFirst();
                                if (shovel.isPresent()) {
                                    index = shovel.get();
                                }
                            }
                        } else {
                            // ��
                            if (validItemLaitMap.containsKey(_PICKAXE)) {
                                Optional<ItemStack> shovel = validItemLaitMap.get(_PICKAXE).stream().filter(i -> ItemUtils.hasDurability(i, 1)).findFirst();
                                if (shovel.isPresent()) {
                                    index = shovel.get();
                                }
                            }
                        }
                        if (index != null) {
                            ItemUtils.useDurability(index, 1);
                            player.giveExp(-PluginConfig.blueprintBreakItemNeedExpCount);
                            // �첽ִ���ھ�Ч��
                            needProcessBlockMap.put(block, index);
                        } else {
                            notBreakSet.add(block.getType());
                        }
                    }
                }
            }
            if (!notBreakSet.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder("[������ͼ] �ھ�");
                notBreakSet.forEach(m -> stringBuilder.append(m.name()).append(", "));
                player.sendMessage(stringBuilder.append("ȱ�ٱ�Ҫ�Ĺ���").toString());
            }
            backItem2Player(player, validItem);
            breakProcess(needProcessBlockMap);
        });
    }

    private static void backItem2Player(Player player, List<ItemStack> validItem) {
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            List<ItemStack> backList = validItem.stream().filter(i -> ItemUtils.hasDurability(i, 1)).collect(Collectors.toList());
            if (!backList.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder("[������ͼ] ���߹黹��");
                backList.forEach(i -> {
                    player.getWorld().dropItem(player.getLocation(), i);
                    stringBuilder.append(i.getType().name()).append("x").append(i.getAmount()).append(", ");
                });
                player.sendMessage(stringBuilder.toString());
            }
        });
    }

    private static void breakProcess(Map<Block, ItemStack> needProcessBlockMap) {
        if (needProcessBlockMap == null || needProcessBlockMap.isEmpty()) return;
        int i = 1;
        for (Map.Entry<Block, ItemStack> entry : needProcessBlockMap.entrySet()) {
            // �ύ�������߳�ִ�У���Ȼ������
            Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(),
                    () -> entry.getKey().breakNaturally(entry.getValue()), 20L * i++);
        }
    }

    /**
     * �첽����
     *
     * @param player
     * @param validItem
     * @param world
     * @param xyzRange
     */
    private static void asyncBuild(Player player, List<ItemStack> validItem, World world, int[] xyzRange) {
        // �����첽������
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PluginCore.getInstance(), () -> {
            Map<Block, Material> needProcessBlockMap = new HashMap<>();
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
                                //�첽������Ч�� block.setType(index.getType());
                                needProcessBlockMap.put(block, index.getType());
                                index.setAmount(index.getAmount() - 1);
                            } else {
                                player.sendMessage("[������ͼ] ���ϲ��㣡�޷�ȫ�����");
                                buildProcess(needProcessBlockMap);
                                return;
                            }
                        }
                    }
                }
            }
            Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
                List<ItemStack> backList = validItem.stream().filter(i -> i.getAmount() > 0).collect(Collectors.toList());
                if (!backList.isEmpty()) {
                    StringBuilder stringBuilder = new StringBuilder("[������ͼ] ���Ϲ黹��");
                    backList.forEach(i -> {
                        player.getWorld().dropItem(player.getLocation(), i);
                        stringBuilder.append(i.getType().name()).append("x").append(i.getAmount()).append(", ");
                    });
                    player.sendMessage(stringBuilder.toString());
                }
            });
            buildProcess(needProcessBlockMap);
        });
    }

    private static void buildProcess(Map<Block, Material> needProcessBlockMap) {
        if (needProcessBlockMap == null || needProcessBlockMap.isEmpty()) return;
        int i = 1;
        for (Map.Entry<Block, Material> entry : needProcessBlockMap.entrySet()) {
            Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
                //entry.getKey().getWorld().spawnFallingBlock(entry.getKey().getLocation().add(0, 50, 0), entry.getValue().createBlockData());
                entry.getKey().setType(entry.getValue());
            }, 10L * i++);
        }
    }
}
