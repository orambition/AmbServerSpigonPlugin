package amb.server.plugin.service.tpb;

import amb.server.plugin.core.PluginCore;
import amb.server.plugin.model.Telepoter;
import amb.server.plugin.service.permission.PermissionConstant;
import amb.server.plugin.service.utils.GUIUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;

import static amb.server.plugin.config.PluginConfig.*;
import static amb.server.plugin.service.tpb.TpBookDataService.*;

public class TpBookService {
    /**
     * 玩家点击传送书事件
     * @param event
     */
    public static void useTpBookEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PermissionConstant.TPB)){
            player.sendMessage("无权限使用!请联系Amb");
            return;
        }
        if (player.isSneaking()) {
            // 前行(Shift+)时，进行快速传送
            event.setCancelled(true);
            doShiftClickAction(player);
        } else {
            Action action = event.getAction();
            // (右击快 && 物体不是可交互的) || 右键空气
            if ((action.equals(Action.RIGHT_CLICK_BLOCK) && !event.getClickedBlock().getType().isInteractable()) || action.equals(Action.RIGHT_CLICK_AIR)){
                event.setCancelled(true);
                TpBookGUI.openBook(event.getPlayer());
            } else if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)){
                event.setCancelled(true);
                TpBookGUI.openMenu(event.getPlayer());
            }
        }
    }
    public static void clickViewMenuEvent(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
            Player player = (Player) event.getWhoClicked();
            if (!player.hasPermission(PermissionConstant.TPB)) {
                player.sendMessage("无权限使用!请联系Amb");
                return;
            }
            ItemStack clickedItem = event.getCurrentItem();
            player.updateInventory();
            player.closeInventory();
            boolean delete = event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && event.getClick().isRightClick();
            boolean setFast = event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && event.getClick().isLeftClick();
            doClickAction(player, clickedItem, delete, setFast);
        }
    }
    /**
     * 玩家shift点击传送书
     * @param player
     */
    private static void doShiftClickAction(Player player){
        Telepoter telepoter = getPrivateFastTeleporter(player.getUniqueId().toString());
        if (telepoter != null) {
            tpPlayerToLocation(player, telepoter.getLocation());
        }else {
            player.sendMessage(ChatColor.RED + "快速传送点为空，无法传送！");
        }
    }
    /**
     * 玩家点击传送菜单
     *
     * @param player
     * @param clickedItem
     */
    private static void doClickAction(Player player, ItemStack clickedItem, boolean delete, boolean setFast) {
        Material type = clickedItem.getType();
        String playerUUID = player.getUniqueId().toString();
        // 自定义变量，用于传递地点编号
        int num = clickedItem.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL);
        Telepoter telepoter = null;
        if (type == publicTpItem) {
            // 公共
            if (delete) {
                delPublicTeleporter(player, num);
                return;
            }
            telepoter = getPublicTeleporterByNum(num);
            if (setFast){
                setFastTeleporter(player, telepoter);
                return;
            }
        } else if (type == privateTpItem) {
            // 私人
            if (delete) {
                delPrivateTeleporter(player, num);
                return;
            }
            telepoter = getPrivateTeleporterByNum(playerUUID, num);
            if (setFast){
                setFastTeleporter(player, telepoter);
                return;
            }
        } else if (type == Material.PLAYER_HEAD) {
            // 在线玩家
            OfflinePlayer offlinePlayer = ((SkullMeta) clickedItem.getItemMeta()).getOwningPlayer();
            if (offlinePlayer.isOnline()) {
                int pSwitch = getTeleporterSwitch(offlinePlayer.getUniqueId().toString());
                if (0 == pSwitch) {
                    player.sendMessage("无法感知到玩家的气息,此玩家没有开启传送");
                } else if (1 == pSwitch) {
                    tpPlayerToPlayer(player, offlinePlayer.getPlayer());
                } /*else if (2 == pSwitch) {
                    requestTpToPlayer(offlinePlayer.getPlayer(), player);
                    player.sendMessage("已开始寻找玩家\n" + ChatColor.GOLD + "等待此玩家回应…");
                }*/
            } else {
                player.sendMessage("玩家已经下线了,无法传送");
            }
            return;
        } else if (type == deadTpItem) {
            // 死亡地点
            telepoter = getDeadTeleporterByNum(playerUUID, num);
        } else if (type == switchTpItem || type == switchOffTpItem) {
            // 传送开关
            setTeleporterSwitch(player);
            return;
        } else if (type == beforeTpItem) {
            // 前一地点
            telepoter = getBeforeTeleporter(playerUUID);
        } else if (type == privateFastTpItem) {
            // 快速传送地点
            telepoter = getPrivateFastTeleporter(playerUUID);
        } else if (type == addPrivateTpItem) {
            addPrivateTeleporter(player, null, num);
            //player.sendMessage(ChatColor.GOLD + "可以使用木牌创建传送点,\n第一行:[addtp]\n第二行:[传送点名称]\n即可添加当前地点为传送点");
            return;
        } else {
            return;
        }
        if (null != telepoter && null != telepoter.getLocation()){
            tpPlayerToLocation(player, telepoter.getLocation());
        } else {
            //player.sendMessage("传送点不存在了,无法传送");
            GUIUtils.sendMsg(player, "传送点不存在,无法传送");
        }

    }

    /**
     * 传送玩家到地点
     *
     * @param player
     * @param location
     */
    private static void tpPlayerToLocation(Player player, Location location) {
        if (doTpPlayer(player, location)) {
            player.playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            GUIUtils.sendMsg(player, "已传送到指定地点");
            //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("已传送至所选地点"));
        }
    }

    /**
     * 传送玩家到玩家
     *
     * @param player
     * @param toPlayer
     */
    public static void tpPlayerToPlayer(Player player, Player toPlayer) {
        if (doTpPlayer(player, toPlayer.getLocation())) {
            toPlayer.sendMessage(ChatColor.GOLD + player.getDisplayName() + "传送至此!");
            toPlayer.playSound(toPlayer.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            GUIUtils.sendMsg(toPlayer, player.getDisplayName() + "传送到此处!");
            player.playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            GUIUtils.sendMsg(player, "传送至" + toPlayer.getDisplayName() + "身边");
        }
        //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("已传送至"+toPlayer.getDisplayName()));
    }

    private static boolean doTpPlayer(Player player, Location location){
        if (costPrice(player)) {
            Telepoter beforeTelepoter = new Telepoter(null, "前一地点", player.getLocation(), 4);
            /*Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                    PluginCore.getInstance(),
                    () -> {},
                    50L
            );*/
            //needTpPet(player, location);
            player.teleport(location);
            TpBookDataService.setBegoreTeleporter(player, beforeTelepoter);
            return true;
        }
        return false;
    }
    /**
     * 传输至玩家时发送请求
     * @param player
     * @param reqPlayer
     */
    private static void requestTpToPlayer(final Player player, final Player reqPlayer) {
        final String reqPlayerName = reqPlayer.getDisplayName();
        if (player.getScoreboardTags().contains("reqTp-" + reqPlayerName)) {
            reqPlayer.sendMessage("正在等待玩家回应……");
            return;
        }
        player.addScoreboardTag("reqTp-" + reqPlayerName);// 传送申请标识
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PluginCore.getInstance(), new Runnable() {
            public void run() {
                if (player.getScoreboardTags().contains("reqTp-" + reqPlayerName)) {
                    player.removeScoreboardTag("reqTp-" + reqPlayerName);
                    player.sendMessage("已拒绝" + reqPlayerName + "的传送请求");
                    reqPlayer.sendMessage("玩家没有响应您的请求");
                }
            }
        }, 200L);
        TextComponent msg = new TextComponent(ChatColor.GREEN + reqPlayerName + "请求传送到您的位置!\n\n");
        TextComponent msg1 = new TextComponent(ChatColor.GOLD + "点击此处[同意/YES]传送\n\n");
        TextComponent msg2 = new TextComponent(ChatColor.RED + "点击此处[拒绝/NO]传送\n");
        TextComponent msg3 = new TextComponent(ChatColor.GREEN + "10s后将默认拒绝传送…");
        // 此命令已删除，此方法已作废，过度设计
        msg1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpbrequest agree " + reqPlayerName));
        msg2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpbrequest deny " + reqPlayerName));
        player.spigot().sendMessage(msg, msg1, msg2, msg3);
        player.sendTitle("", ChatColor.GREEN + reqPlayerName + "请求传送到您的位置", 10, 70, 20);
        //sendMsg(player,reqPlayerName+"请求传送到您的位置");
    }

    /**
     * 设置传送开关
     */
    public static void setTeleporterSwitch(Player player) {
        String path = "player." + player.getUniqueId().toString() + ".switch";
        // 0 禁用、1开启、2开启但需要审核
        if (tpbSaveData.contains(path)) {
            int num = (tpbSaveData.getInt(path) + 1) % 2;
            tpbSaveData.set(path, num);
            String str = "";
            if (num == 0) {
                str = "已将开关设置为:" + ChatColor.RED + "禁止所有人传送到身边";
            } else if (num == 1) {
                str = "已将开关设置为:" + ChatColor.GOLD + "允许所有人传送到身边";
            } /*else {
                str = "已将开关设置为:" + ChatColor.GOLD + "需要审批才能传送到身边";
            }*/
            player.sendMessage(str);
        } else {
            tpbSaveData.set(path, 1);
            player.sendMessage("已将开关设置为:" + ChatColor.GOLD + "允许所有人传送到身边");
        }
        saveTpbSaveData();
    }

    /**
     * 获取指定玩的的传送开关
     *
     * @param uuid
     * @return
     */
    public static int getTeleporterSwitch(String uuid) {
        String path = "player." + uuid + ".switch";
        return tpbSaveData.getInt(path, 0);
    }

    /**
     * 校验是否可以传送至此玩家
     *
     * @param uuid
     * @return
     */
    public static boolean checkPlayerCanBeTp(String uuid) {
        String path = "player." + uuid + ".cantp";
        return tpbSaveData.getBoolean(path, true);
    }

    /**
     * 设置是否可以传送至此玩家
     *
     * @param uuid
     * @return
     */
    public static void setPlayerCanBeTp(String uuid, boolean canTp) {
        String path = "player." + uuid + ".cantp";
        tpbSaveData.set(path, canTp);
        saveTpbSaveData();
    }

    /**
     * 传送消耗
     *
     * @param player
     * @return
     */
    private static boolean costPrice(Player player) {
        int price = tpBookTpPrice;
        if (player.getInventory().contains(tpBookCurrencyItem, price)) {
            player.getInventory().removeItem(new ItemStack(tpBookCurrencyItem, price));
            player.sendMessage(ChatColor.GOLD + "消耗[" + tpBookCurrencyItemName + "x" + price + "]");
            return true;
        } else {
            ItemStack tpBook = player.getInventory().getItemInMainHand();
            ItemMeta itemMeta = tpBook.getItemMeta();
            int page = itemMeta.getEnchantLevel(Enchantment.DAMAGE_ALL);
            if (page > price) {
                itemMeta.setLore(TpBookItem.getBookItemLore(page - price));
                itemMeta.addEnchant(Enchantment.DAMAGE_ALL, page - price, true);
                tpBook.setItemMeta(itemMeta);
                player.getInventory().setItemInMainHand(tpBook);
                player.sendMessage("背包中[" + tpBookCurrencyItemName + "]不足" + price + "颗!\n" + ChatColor.RED + "已消耗" + price + "页 传送书");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "传送书 -" + price));
            } else if (!player.getDisplayName().equals("LadyJv")) {
                player.getInventory().setItemInMainHand(null);
                player.sendMessage("背包中[" + tpBookCurrencyItemName + "]不足" + price + "颗!\n" + ChatColor.RED + "传送书用完了!!!");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "传送书用完了"));
                player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }
            return true;
        }
    }

    /**
     * 传输玩家宠物
     * 需要先于玩家传送，以防止区块卸载
     * @param player
     * @param location
     */
    private static void needTpPet(Player player, Location location){
        if (!player.getWorld().equals(location.getWorld())){
            return;
        }
        Collection<Entity> pets = player.getWorld().getEntitiesByClasses(Tameable.class);
        pets.forEach(e->{
            if (((Tameable)e).isTamed()
                    && !((Sittable)e).isSitting()
                    && ((Tameable)e).getOwner().getUniqueId().equals(player.getUniqueId())){
                e.teleport(location);
                player.sendMessage("tp pet");
            }
        });
    }
}
