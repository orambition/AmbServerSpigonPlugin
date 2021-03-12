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
     * ʹ�� ��ͼѡ����
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

            // Ǳ�� ��ģʽѡ��˵�
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
                player.sendMessage("[������ͼ] ��ͨ�� [Ǳ��+�ھ�][Shift+���] �л�����ģʽ");
            }
            // Ǳ�� ����ճ��ģʽ ����
            else if (MENU_ITEM_COPY.name.equals(modeName) && player.isSneaking()) {
                event.setCancelled(true);
                CopyMode.doShiftUseEvent(player);
            }
            // �Ҽ�����
            else if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
                invokeMode(modeName, true, player, event.getClickedBlock().getLocation());
            }
            // �������
            else if (action.equals(Action.LEFT_CLICK_BLOCK)) {
                invokeMode(modeName, false, player, event.getClickedBlock().getLocation());
            }
        }
    }

    /**
     * ��ģʽѡ�����
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
     * ģʽ����ѡ�����
     * @param event
     */
    public static void doClickMenuEvent(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();
        // �л�ģʽʱ ���ѡ��
        BlueprintUtil.delSelected(player);

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("[������ͼ]-" + clickedItem.getItemMeta().getDisplayName());
        itemMeta.setLore(clickedItem.getItemMeta().getLore());
        itemStack.setItemMeta(itemMeta);
        player.sendMessage("[������ͼ] ģʽ�л��ɹ�");
    }

    /**
     * ִ��ģʽ��ѡ�񷽷�����
     * @param modeName
     */
    private static void invokeMode(String modeName, boolean isRight, Player player, Location location) {
        // ���ýӿڼӶ�̬ʵ�֣���Ϊ���� new �����˷��ڴ�
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
        MENU_ITEM_FILLING(Material.WHITE_WOOL, ChatColor.GOLD + "�������", Arrays.asList("���/�Ҽ���ѡĿ���", "����������")),
        MENU_ITEM_BATCH_PUT(Material.GRAY_WOOL, ChatColor.GOLD + "��������", Arrays.asList("���ѡ�����Ŀ��", "�Ҽ�ѡ����÷���")),
        MENU_ITEM_COPY(Material.BLUE_WOOL, ChatColor.GOLD + "����/ճ��", Arrays.asList()),
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
     * �Զ��ھ���ƻ���Ϸƽ�⣬û�г���ʹ�ã�
     *
     * @param player
     * @param validItem
     * @param world
     * @param xyzRange
     */
    /*private static void asyncBreak(Player player, List<ItemStack> validItem, World world, int[] xyzRange) {
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
        // �ύ�������߳�ִ�У���Ȼ������
        Bukkit.getServer().getScheduler().runTaskLater(PluginCore.getInstance(), () -> {
            for (Map.Entry<Block, ItemStack> entry : needProcessBlockMap.entrySet()) {
                entry.getKey().breakNaturally(entry.getValue());
            }
        }, 20L);
    }*/
}
