package amb.server.plugin.service.blueprint;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.service.blueprint.mode.BatchPutMode;
import amb.server.plugin.service.blueprint.mode.CopyMode;
import amb.server.plugin.service.blueprint.mode.FillingMode;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.utils.ItemUtils;
import amb.server.plugin.service.utils.PlayerUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginLogger;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static amb.server.plugin.service.blueprint.BlueprintService.ModeMenuItem.*;
import static amb.server.plugin.service.utils.PlayerUtils.PLAYER_BLUEPRINT_SELECT;

/**
 * @author zhangrenjing
 * created on 2021/3/6
 */
public class BlueprintService {
    private static final Logger LOGGER = PluginLogger.getLogger("Ambition");

    /**
     * 使用 蓝图选择器
     *
     * @param event
     */
    public static void useSelectorEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(PermissionConstant.BLUEPRINT)
                && PlayerUtils.notMark(player, PLAYER_BLUEPRINT_SELECT)) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType().isInteractable()) {
                return;
            }
            Action action = event.getAction();

            // 潜行 打开模式选择菜单
            if (player.isSneaking() && (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK))) {
                event.setCancelled(true);
                openModeSelectMenu(player);
                return;
            }
            String modeName = null;
            String[] str = event.getItem().getItemMeta().getDisplayName().split("-");
            if (str.length > 1 ) {
                modeName = str[1];
            }
            if (StringUtils.isBlank(modeName)) {
                player.sendMessage("[建筑蓝图] 请通过 [潜行+挖掘][Shift+左键] 切换建筑模式");
            }
            // 潜行 复制粘贴模式 独有
            else if (MENU_ITEM_COPY.name.equals(modeName) && player.isSneaking()) {
                event.setCancelled(true);
                CopyMode.doShiftUseEvent(player);
            }
            // 右键方块
            else if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                invokeMode(modeName, true, player, event.getClickedBlock().getLocation());
            }
            // 左键方块
            else if (action.equals(Action.LEFT_CLICK_BLOCK)) {
                invokeMode(modeName, false, player, event.getClickedBlock().getLocation());
            }
        }
    }

    /**
     * 打开模式选择界面
     * @param player
     */
    private static void openModeSelectMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, PluginConfig.blueprintModeSelectMenu);
        for (ModeMenuItem menuItem : ModeMenuItem.values()) {
            inventory.addItem(ItemUtils.buildMenuItem(menuItem.material, menuItem.name, menuItem.loreList));
        }
        player.openInventory(inventory);
    }

    /**
     * 模式界面选择操作
     * @param event
     */
    public static void doClickMenuEvent(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();
        // 切换模式时 清空选择
        BlueprintUtil.delSelected(player);

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("[建筑蓝图]-" + clickedItem.getItemMeta().getDisplayName());
        itemMeta.setLore(clickedItem.getItemMeta().getLore());
        itemStack.setItemMeta(itemMeta);
        player.sendMessage("[建筑蓝图] 模式切换成功");
    }

    /**
     * 执行模式的选择方法方法
     * @param modeName
     */
    private static void invokeMode(String modeName, boolean isRight, Player player, Location location) {
        // 不用接口加多态实现，因为不想 new 对象浪费内存
        if (MENU_ITEM_FILLING.name.equals(modeName)) {
            if (isRight) {
                FillingMode.doUseEvent(player, location);
            } else {
                FillingMode.doTouchEvent(player, location);
            }
        } else if (MENU_ITEM_BATCH_PUT.name.equals(modeName)) {
            if (isRight) {
                BatchPutMode.doUseEvent(player, location);
            } else {
                BatchPutMode.doTouchEvent(player, location);
            }
        } else if (MENU_ITEM_COPY.name.equals(modeName)) {
            if (isRight) {
                CopyMode.doUseEvent(player, location);
            } else {
                CopyMode.doTouchEvent(player, location);
            }
        }
    }

    enum ModeMenuItem{
        MENU_ITEM_FILLING(Material.WHITE_WOOL, ChatColor.GOLD + "填充区域", Arrays.asList("左键/右键框选目标后", "放入填充材料")),
        MENU_ITEM_BATCH_PUT(Material.GRAY_WOOL, ChatColor.GOLD + "批量放置", Arrays.asList("左键选择放置目标", "右键选择放置方向")),
        MENU_ITEM_COPY(Material.BLUE_WOOL, ChatColor.GOLD + "复制/粘贴", Arrays.asList()),
        ;

        private Material material;
        private String name;
        private List<String> loreList;

        ModeMenuItem(Material material, String name, List<String> loreList) {
            this.material = material;
            this.name = name;
            this.loreList = loreList;
        }
    }

    /**
     * 自动挖掘会破坏游戏平衡，没有场景使用，
     *
     * @param player
     * @param validItem
     * @param world
     * @param xyzRange
     */
    /*private static void asyncBreak(Player player, List<ItemStack> validItem, World world, int[] xyzRange) {
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
        // 提交任务到主线程执行，不然有问题
        Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
            for (Map.Entry<Block, ItemStack> entry : needProcessBlockMap.entrySet()) {
                entry.getKey().breakNaturally(entry.getValue());
            }
        }, 20L);
    }*/
}
