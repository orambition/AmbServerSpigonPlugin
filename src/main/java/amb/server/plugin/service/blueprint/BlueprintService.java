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
     * 使用 蓝图选择器
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
                        // 前行(Shift+) 为破坏操作
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
     * 填充预处理，仅做校验，打开材料/工具填充界面，
     * 监听页面关闭事件，开始填充/破坏
     *
     * @param player
     * @param pos1
     * @param pos2
     * @param isBreak 是否为破坏操作
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
            player.sendMessage("[建筑蓝图] 选择范围过大，请重新选择！");
            return;
        }
        // 加串行锁，同时只能执行一个任务
        PlayerUtils.mark(player, PLAYER_BLUEPRINT_SELECT);
        // 绘制粒子
        ParticleUtils.drawLineTimer(pos1, pos2);

        if (isBreak) {
            // 破坏，这个方法是当前线程延时执行，不是真正的"异步"的，背包无法在异步线程中打开
            Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
                Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.blueprintBreakItemPutName);
                player.openInventory(inventory);
            }, 30);
            GUIUtils.sendMsg(player, "区域选择成功，请放入工具");
            player.sendMessage("[建筑蓝图] 区域选择成功，请放入[工具]. (挖掘将消耗经验值)");
        } else {
            // 建造
            Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
                Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.blueprintBuildItemPutName);
                player.openInventory(inventory);
            }, 30);
            GUIUtils.sendMsg(player, "区域选择成功，请放入建造材料");
            player.sendMessage("[建筑蓝图] 区域选择成功，请放入建造[材料]");
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
            player.sendMessage("[建筑蓝图] 选择位置无效！");
            return;
        }
        BlueprintUtil.delSelected(player);
        int[] xyzRange = BlueprintUtil.getRange(pos1, pos2);
        World world = pos1.getWorld();

        if (isBreak) {
            // 破坏
            asyncBreak(player, validItem, world, xyzRange);
        } else {
            // 填充
            asyncBuild(player, validItem, world, xyzRange);
        }

    }

    /**
     * 异步破坏
     *
     * @param player
     * @param validItem
     * @param world
     * @param xyzRange
     */
    private static void asyncBreak(Player player, List<ItemStack> validItem, World world, int[] xyzRange) {
        // 异步运行，但需要提交任务到主线程进行同步执行
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
                            player.sendMessage("[建筑蓝图] 经验不足，无法使用蓝图挖掘");
                            backItem2Player(player, validItem);
                            breakProcess(needProcessBlockMap);
                            return;
                        }
                        ItemStack index = null;
                        if (block.getType() == Material.GRASS_BLOCK || block.getType() == Material.DIRT
                                || block.getType() == Material.COARSE_DIRT || block.getType() == Material.PODZOL
                                || block.getType() == Material.SAND || block.getType() == Material.GRAVEL) {
                            // 锹
                            if (validItemLaitMap.containsKey(_SHOVEL)) {
                                Optional<ItemStack> shovel = validItemLaitMap.get(_SHOVEL).stream().filter(i -> ItemUtils.hasDurability(i, 1)).findFirst();
                                if (shovel.isPresent()) {
                                    index = shovel.get();
                                }
                            }
                        } else if (block.getType().name().contains("_WOOD")
                                || block.getType().name().contains("_LOG")
                                || block.getType().name().contains("_PLANKS")) {
                            // 斧
                            if (validItemLaitMap.containsKey(_AXE)) {
                                Optional<ItemStack> shovel = validItemLaitMap.get(_AXE).stream().filter(i -> ItemUtils.hasDurability(i, 1)).findFirst();
                                if (shovel.isPresent()) {
                                    index = shovel.get();
                                }
                            }
                        } else {
                            // 镐
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
                            // 异步执行挖掘效果
                            needProcessBlockMap.put(block, index);
                        } else {
                            notBreakSet.add(block.getType());
                        }
                    }
                }
            }
            if (!notBreakSet.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder("[建筑蓝图] 挖掘");
                notBreakSet.forEach(m -> stringBuilder.append(m.name()).append(", "));
                player.sendMessage(stringBuilder.append("缺少必要的工具").toString());
            }
            backItem2Player(player, validItem);
            breakProcess(needProcessBlockMap);
        });
    }

    private static void backItem2Player(Player player, List<ItemStack> validItem) {
        Bukkit.getServer().getScheduler().runTask(PluginCore.getInstance(), () -> {
            List<ItemStack> backList = validItem.stream().filter(i -> ItemUtils.hasDurability(i, 1)).collect(Collectors.toList());
            if (!backList.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder("[建筑蓝图] 工具归还：");
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
            // 提交任务到主线程执行，不然有问题
            Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(),
                    () -> entry.getKey().breakNaturally(entry.getValue()), 20L * i++);
        }
    }

    /**
     * 异步建造
     *
     * @param player
     * @param validItem
     * @param world
     * @param xyzRange
     */
    private static void asyncBuild(Player player, List<ItemStack> validItem, World world, int[] xyzRange) {
        // 不能异步操作块
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
                                //异步处理构建效果 block.setType(index.getType());
                                needProcessBlockMap.put(block, index.getType());
                                index.setAmount(index.getAmount() - 1);
                            } else {
                                player.sendMessage("[建筑蓝图] 材料不足！无法全部填充");
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
                    StringBuilder stringBuilder = new StringBuilder("[建筑蓝图] 材料归还：");
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
