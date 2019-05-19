package amb.server.plugin.service.tpb;

import amb.server.plugin.core.PluginCore;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.Collections;

import static amb.server.plugin.config.PluginConfig.*;
import static amb.server.plugin.service.tpb.TpBookDataService.*;

public class TpBookService {
    /**
     * �������
     *
     * @param player
     * @param clickedItem
     */
    public static void doClickAction(Player player, ItemStack clickedItem, boolean delete) {
        Material type = clickedItem.getType();
        // �Զ�����������ڴ��ݵص���
        int num = clickedItem.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL);
        Location location = null;
        if (type == publicTpItem) {
            // ����
            if (delete) {
                delPublicTeleporter(player, num);
                return;
            }
            location = getPublicTeleporterByNum(num);
        } else if (type == privateTpItem) {
            // ˽��
            if (delete) {
                delPrivateTeleporter(player, num);
                return;
            }
            location = getPrivateTeleporterByNum(player.getUniqueId().toString(), num);
        } else if (type == Material.PLAYER_HEAD) {
            // �������
            OfflinePlayer offlinePlayer = ((SkullMeta) clickedItem.getItemMeta()).getOwningPlayer();
            if (offlinePlayer.isOnline()) {
                int pSwitch = getTeleporterSwitch(offlinePlayer.getUniqueId().toString());
                if (0 == pSwitch) {
                    player.sendMessage("�޷���֪����ҵ���Ϣ,�����û�п�������");
                } else if (1 == pSwitch) {
                    tpPlayerToPlayer(player, offlinePlayer.getPlayer());
                } else if (2 == pSwitch) {
                    requestTpToPlayer(offlinePlayer.getPlayer(),player);
                    player.sendMessage("�ѿ�ʼѰ�����\n" + ChatColor.GOLD + "�ȴ�����һ�Ӧ��");
                }
            } else {
                player.sendMessage("����Ѿ�������,�޷�����");
            }
            return;
        } else if (type == deadTpItem) {
            // �����ص�
            location = getDeadTeleporterByNum(player.getUniqueId().toString(), num);
        } else if (type == switchTpItem || type == switchOffTpItem) {
            // ���Ϳ���
            setTeleporterSwitch(player);
            return;
        } else if (type == addPrivateTpItem) {
            addPrivateTeleporter(player, null, num);
            //player.sendMessage(ChatColor.GOLD + "����ʹ��ľ�ƴ������͵�,\n��һ��:[addtp]\n�ڶ���:[���͵�����]\n������ӵ�ǰ�ص�Ϊ���͵�");
            return;
        }
        if (null != location) {
            if (costPrice(player)) {
                player.teleport(location);
                player.playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                sendMsg(player, "�Ѵ��͵�ָ���ص�");
                //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("�Ѵ�������ѡ�ص�"));
            }
        } else {
            //player.sendMessage("���͵㲻������,�޷�����");
            sendMsg(player, "���͵㲻������,�޷�����");
        }
    }

    private static void requestTpToPlayer(final Player player, final Player reqPlayer){
        final String reqPlayerName = reqPlayer.getDisplayName();
        if (player.getScoreboardTags().contains("reqTp-"+reqPlayerName)){
            reqPlayer.sendMessage("���ڵȴ���һ�Ӧ����");
            return;
        }
        player.addScoreboardTag("reqTp-"+reqPlayerName);// ���������ʶ
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PluginCore.getInstance(), new Runnable() {
            public void run() {
                if (player.getScoreboardTags().contains("reqTp-"+reqPlayerName)){
                    player.removeScoreboardTag("reqTp-"+reqPlayerName);
                    player.sendMessage("�Ѿܾ�"+reqPlayerName +"�Ĵ�������");
                    reqPlayer.sendMessage("���û����Ӧ��������");
                }
            }
        },200L);
        TextComponent msg = new TextComponent(ChatColor.GREEN + reqPlayerName+"�����͵�����λ��!\n\n");
        TextComponent msg1 = new TextComponent(ChatColor.GOLD + "����˴�[ͬ��/YES]����\n\n");
        TextComponent msg2 = new TextComponent(ChatColor.RED + "����˴�[�ܾ�/NO]����\n");
        TextComponent msg3 = new TextComponent(ChatColor.GREEN + "10s��Ĭ�Ͼܾ����͡�");

        msg1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/tpbrequest agree "+reqPlayerName));
        msg2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/tpbrequest deny "+reqPlayerName));
        player.spigot().sendMessage(msg,msg1,msg2,msg3);
        player.sendTitle("",ChatColor.GREEN + reqPlayerName+"�����͵�����λ��",10,70,20);
        //sendMsg(player,reqPlayerName+"�����͵�����λ��");
    }

    /**
     * ������ҵ����
     *
     * @param player
     * @param toPlayer
     */
    public static void tpPlayerToPlayer(Player player, Player toPlayer) {
        if (costPrice(player)) {
            player.teleport(toPlayer);
            toPlayer.sendMessage(ChatColor.GOLD + player.getDisplayName() + "��������!");
            toPlayer.playSound(toPlayer.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            sendMsg(toPlayer, player.getDisplayName() + "���͵��˴�!");
            player.playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            sendMsg(player, "������" + toPlayer.getDisplayName() + "���");
        }
        //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("�Ѵ�����"+toPlayer.getDisplayName()));
    }

    /**
     * ���ô��Ϳ���
     */
    public static void setTeleporterSwitch(Player player) {
        String path = "player." + player.getUniqueId().toString() + ".switch";
        // 0 ���á�1������2��������Ҫ���
        if (tpbSaveData.contains(path)) {
            int num = (tpbSaveData.getInt(path) + 1) % (player.hasPermission("op")?3:2);
            tpbSaveData.set(path, num);
            String str;
            if (num == 0) {
                str = "�ѽ���������Ϊ:" + ChatColor.RED + "��ֹ�����˴��͵����";
            } else if (num == 1) {
                str = "�ѽ���������Ϊ:" + ChatColor.GOLD + "���������˴��͵����";
            } else {
                str = "�ѽ���������Ϊ:" + ChatColor.GOLD + "��Ҫ�������ܴ��͵����";
            }
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
                itemMeta.setLore(Collections.singletonList(ChatColor.RESET + "ҳ��:" + (page - price)));
                itemMeta.addEnchant(Enchantment.DAMAGE_ALL, page - price, true);
                tpBook.setItemMeta(itemMeta);
                player.getInventory().setItemInMainHand(tpBook);
                player.sendMessage("������[" + tpBookCurrencyItemName + "]����" + price + "��!\n" + ChatColor.RED + "������" + price + "ҳ ������");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED+"������ -" + price));
            } else {
                player.getInventory().setItemInMainHand(null);
                player.sendMessage("������[" + tpBookCurrencyItemName + "]����" + price + "��!\n" + ChatColor.RED + "������������!!!");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED+"������������"));
                player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }
            return true;
        }
    }
}
