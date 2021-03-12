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
     * ��ҵ���������¼�
     *
     * @param event
     */
    public static void useTpBookEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PermissionConstant.TPB)) {
            player.sendMessage("��Ȩ��ʹ��!����ϵAmb");
            return;
        }
        if (player.isSneaking()) {
            // ǰ��(Shift+)ʱ�����п��ٴ���
            event.setCancelled(true);
            doShiftClickAction(player);
        } else {
            Action action = event.getAction();
            // (�һ��� && ���岻�ǿɽ�����) || �Ҽ�����
            if ((action.equals(Action.RIGHT_CLICK_BLOCK) && !event.getClickedBlock().getType().isInteractable()) || action.equals(Action.RIGHT_CLICK_AIR)) {
                event.setCancelled(true);
                TpBookGUI.openBook(event.getPlayer());
            } else if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)) {
                event.setCancelled(true);
                TpBookGUI.openMenu(event.getPlayer());
            }
        }
    }

    public static void clickViewMenuEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission(PermissionConstant.TPB)) {
            player.sendMessage("��Ȩ��ʹ��!����ϵAmb");
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        player.updateInventory();
        player.closeInventory();
        boolean delete = event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && event.getClick().isRightClick();
        boolean setFast = event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && event.getClick().isLeftClick();
        doClickAction(player, clickedItem, delete, setFast);
    }

    /**
     * ���shift���������
     *
     * @param player
     */
    private static void doShiftClickAction(Player player) {
        Telepoter telepoter = getPrivateFastTeleporter(player.getUniqueId().toString());
        if (telepoter != null) {
            tpPlayerToLocation(player, telepoter.getLocation());
        } else {
            player.sendMessage(ChatColor.RED + "���ٴ��͵�Ϊ�գ��޷����ͣ�");
        }
    }

    /**
     * ��ҵ�����Ͳ˵�
     *
     * @param player
     * @param clickedItem
     */
    private static void doClickAction(Player player, ItemStack clickedItem, boolean delete, boolean setFast) {
        Material type = clickedItem.getType();
        String playerUUID = player.getUniqueId().toString();
        // �Զ�����������ڴ��ݵص���
        int num = clickedItem.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL);
        Telepoter telepoter = null;
        if (type == publicTpItem) {
            // ����
            if (delete) {
                delPublicTeleporter(player, num);
                return;
            }
            telepoter = getPublicTeleporterByNum(num);
            if (setFast) {
                setFastTeleporter(player, telepoter);
                return;
            }
        } else if (type == privateTpItem) {
            // ˽��
            if (delete) {
                delPrivateTeleporter(player, num);
                return;
            }
            telepoter = getPrivateTeleporterByNum(playerUUID, num);
            if (setFast) {
                setFastTeleporter(player, telepoter);
                return;
            }
        } else if (type == Material.PLAYER_HEAD) {
            // �������
            OfflinePlayer offlinePlayer = ((SkullMeta) clickedItem.getItemMeta()).getOwningPlayer();
            if (offlinePlayer.isOnline()) {
                int pSwitch = getTeleporterSwitch(offlinePlayer.getUniqueId().toString());
                if (0 == pSwitch) {
                    player.sendMessage("�޷���֪����ҵ���Ϣ,�����û�п�������");
                } else if (1 == pSwitch) {
                    tpPlayerToPlayer(player, offlinePlayer.getPlayer());
                } /*else if (2 == pSwitch) {
                    requestTpToPlayer(offlinePlayer.getPlayer(), player);
                    player.sendMessage("�ѿ�ʼѰ�����\n" + ChatColor.GOLD + "�ȴ�����һ�Ӧ��");
                }*/
            } else {
                player.sendMessage("����Ѿ�������,�޷�����");
            }
            return;
        } else if (type == deadTpItem) {
            // �����ص�
            telepoter = getDeadTeleporterByNum(playerUUID, num);
        } else if (type == switchTpItem || type == switchOffTpItem) {
            // ���Ϳ���
            setTeleporterSwitch(player);
            return;
        } else if (type == beforeTpItem) {
            // ǰһ�ص�
            telepoter = getBeforeTeleporter(playerUUID);
        } else if (type == privateFastTpItem) {
            // ���ٴ��͵ص�
            telepoter = getPrivateFastTeleporter(playerUUID);
        } else if (type == addPrivateTpItem) {
            addPrivateTeleporter(player, null, num);
            //player.sendMessage(ChatColor.GOLD + "����ʹ��ľ�ƴ������͵�,\n��һ��:[addtp]\n�ڶ���:[���͵�����]\n������ӵ�ǰ�ص�Ϊ���͵�");
            return;
        } else {
            return;
        }
        if (null != telepoter && null != telepoter.getLocation()) {
            tpPlayerToLocation(player, telepoter.getLocation());
        } else {
            //player.sendMessage("���͵㲻������,�޷�����");
            GUIUtils.sendMsg(player, "���͵㲻����,�޷�����");
        }

    }

    /**
     * ������ҵ��ص�
     *
     * @param player
     * @param location
     */
    private static void tpPlayerToLocation(Player player, Location location) {
        if (doTpPlayer(player, location)) {
            player.playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            GUIUtils.sendMsg(player, "�Ѵ��͵�ָ���ص�");
            //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("�Ѵ�������ѡ�ص�"));
        }
    }

    /**
     * ������ҵ����
     *
     * @param player
     * @param toPlayer
     */
    public static void tpPlayerToPlayer(Player player, Player toPlayer) {
        if (doTpPlayer(player, toPlayer.getLocation())) {
            toPlayer.sendMessage(ChatColor.GOLD + player.getDisplayName() + "��������!");
            toPlayer.playSound(toPlayer.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            GUIUtils.sendMsg(toPlayer, player.getDisplayName() + "���͵��˴�!");
            player.playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            GUIUtils.sendMsg(player, "������" + toPlayer.getDisplayName() + "���");
        }
        //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("�Ѵ�����"+toPlayer.getDisplayName()));
    }

    private static boolean doTpPlayer(Player player, Location location) {
        if (costPrice(player)) {
            Telepoter beforeTelepoter = new Telepoter(null, "ǰһ�ص�", player.getLocation(), 4);
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
     * ���������ʱ��������
     *
     * @param player
     * @param reqPlayer
     */
    private static void requestTpToPlayer(final Player player, final Player reqPlayer) {
        final String reqPlayerName = reqPlayer.getDisplayName();
        if (player.getScoreboardTags().contains("reqTp-" + reqPlayerName)) {
            reqPlayer.sendMessage("���ڵȴ���һ�Ӧ����");
            return;
        }
        player.addScoreboardTag("reqTp-" + reqPlayerName);// ���������ʶ
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PluginCore.getInstance(), new Runnable() {
            public void run() {
                if (player.getScoreboardTags().contains("reqTp-" + reqPlayerName)) {
                    player.removeScoreboardTag("reqTp-" + reqPlayerName);
                    player.sendMessage("�Ѿܾ�" + reqPlayerName + "�Ĵ�������");
                    reqPlayer.sendMessage("���û����Ӧ��������");
                }
            }
        }, 200L);
        TextComponent msg = new TextComponent(ChatColor.GREEN + reqPlayerName + "�����͵�����λ��!\n\n");
        TextComponent msg1 = new TextComponent(ChatColor.GOLD + "����˴�[ͬ��/YES]����\n\n");
        TextComponent msg2 = new TextComponent(ChatColor.RED + "����˴�[�ܾ�/NO]����\n");
        TextComponent msg3 = new TextComponent(ChatColor.GREEN + "10s��Ĭ�Ͼܾ����͡�");
        // ��������ɾ�����˷��������ϣ��������
        msg1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpbrequest agree " + reqPlayerName));
        msg2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpbrequest deny " + reqPlayerName));
        player.spigot().sendMessage(msg, msg1, msg2, msg3);
        player.sendTitle("", ChatColor.GREEN + reqPlayerName + "�����͵�����λ��", 10, 70, 20);
        //sendMsg(player,reqPlayerName+"�����͵�����λ��");
    }

    /**
     * ���ô��Ϳ���
     */
    public static void setTeleporterSwitch(Player player) {
        String path = "player." + player.getUniqueId().toString() + ".switch";
        // 0 ���á�1������2��������Ҫ���
        if (tpbSaveData.contains(path)) {
            int num = (tpbSaveData.getInt(path) + 1) % 2;
            tpbSaveData.set(path, num);
            String str = "";
            if (num == 0) {
                str = "�ѽ���������Ϊ:" + ChatColor.RED + "��ֹ�����˴��͵����";
            } else if (num == 1) {
                str = "�ѽ���������Ϊ:" + ChatColor.GOLD + "���������˴��͵����";
            } /*else {
                str = "�ѽ���������Ϊ:" + ChatColor.GOLD + "��Ҫ�������ܴ��͵����";
            }*/
            player.sendMessage(str);
        } else {
            tpbSaveData.set(path, 1);
            player.sendMessage("�ѽ���������Ϊ:" + ChatColor.GOLD + "���������˴��͵����");
        }
        saveTpbSaveData();
    }

    /**
     * ��ȡָ����ĵĴ��Ϳ���
     *
     * @param uuid
     * @return
     */
    public static int getTeleporterSwitch(String uuid) {
        String path = "player." + uuid + ".switch";
        return tpbSaveData.getInt(path, 0);
    }

    /**
     * У���Ƿ���Դ����������
     *
     * @param uuid
     * @return
     */
    public static boolean checkPlayerCanBeTp(String uuid) {
        String path = "player." + uuid + ".cantp";
        return tpbSaveData.getBoolean(path, true);
    }

    /**
     * �����Ƿ���Դ����������
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
     * ��������
     *
     * @param player
     * @return
     */
    private static boolean costPrice(Player player) {
        int price = tpBookTpPrice;
        if (player.getInventory().contains(tpBookCurrencyItem, price)) {
            player.getInventory().removeItem(new ItemStack(tpBookCurrencyItem, price));
            player.sendMessage(ChatColor.GOLD + "����[" + tpBookCurrencyItemName + "x" + price + "]");
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
                player.sendMessage("������[" + tpBookCurrencyItemName + "]����" + price + "��!\n" + ChatColor.RED + "������" + price + "ҳ ������");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "������ -" + price));
            } else if (!player.getDisplayName().equals("LadyJv")) {
                player.getInventory().setItemInMainHand(null);
                player.sendMessage("������[" + tpBookCurrencyItemName + "]����" + price + "��!\n" + ChatColor.RED + "������������!!!");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "������������"));
                player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }
            return true;
        }
    }

    /**
     * ������ҳ���
     * ��Ҫ������Ҵ��ͣ��Է�ֹ����ж��
     *
     * @param player
     * @param location
     */
    private static void needTpPet(Player player, Location location) {
        if (!player.getWorld().equals(location.getWorld())) {
            return;
        }
        Collection<Entity> pets = player.getWorld().getEntitiesByClasses(Tameable.class);
        pets.forEach(e -> {
            if (((Tameable) e).isTamed()
                    && !((Sittable) e).isSitting()
                    && ((Tameable) e).getOwner().getUniqueId().equals(player.getUniqueId())) {
                e.teleport(location);
                player.sendMessage("tp pet");
            }
        });
    }
}
