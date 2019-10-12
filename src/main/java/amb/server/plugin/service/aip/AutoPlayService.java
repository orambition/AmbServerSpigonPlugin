package amb.server.plugin.service.aip;

import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.service.aip.navigation.AutoPlayNavigator;
import amb.server.plugin.tools.NMSUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class AutoPlayService {
    private static final Logger logger = PluginLogger.getLogger(AutoPlayService.class.getName());
    /**
     * ����һ�� aiʵ��
     * @param location
     * @return
     */
    public static Friday addAutoPlayer(String name, UUID uuid, Location location) {
        // ��ȡ��ַ������nmsWorld
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        // �����������
        GameProfile profile = new GameProfile(uuid, name);
        // ������ҽ���������
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(nmsWorld);
        // �����Զ���nmsʵ��
        Friday friday = new Friday(nmsWorld.getServer().getServer(), nmsWorld, profile, playerInteractManager);

        // ͨ���˷��������һ����ʵ��ң��滻Mr_Amb�����ô˷���
        //nmsWorld.getServer().getHandle().a(friday.playerConnection.networkManager, friday);

        // ��nmsʵ�� ת��Ϊbukkitʵ��
        Player playerFriday = friday.getBukkitEntity();
        // ���Զ���ʵ����ӵ�����
        boolean addEntitySuccess = NMSUtil.addEntityToWorld(playerFriday, location);
        if (addEntitySuccess){
            // ���������������(�ɼ�)��ҷ���ʵ����Ϣ
            playerFriday.getWorld().getPlayers().forEach(player -> NMSUtil.sendTabListAdd(player, playerFriday));
            // ���Զ���ʵ����뵽�������������б���Ȼ��Ϸ�߼�����Ч���磺���﹥��
            NMSUtil.addOrRemoveFromPlayerList(playerFriday, false);
            logger.info("[Friday] Add Friday at " + playerFriday.getLocation().toString());
            return friday;
        } else {
            logger.info("[Friday] Add Friday Failed ");
        }

        return null;
    }

    /**
     * ��ʼ�� ai ʵ��
     * @param friday
     * @return
     */
    public static Friday initAutoPlayer(Friday friday, Player player){
        // ��ʼ��װ��
        if (friday != null) {
            PlayerInventory inventory = friday.getBukkitEntity().getInventory();
            ItemStack itemStack = new ItemStack(Material.BARRIER);
            for (int i=0; i < 9; i++){
                inventory.setItem(i, itemStack);
            }
            if (player != null){
                // �·�
                inventory.setArmorContents(player.getInventory().getArmorContents());
                // ������
                inventory.setItemInMainHand(player.getInventory().getItemInMainHand());
                inventory.setItemInOffHand(player.getInventory().getItemInOffHand());
            } else {
                // �·�
                org.bukkit.inventory.ItemStack[] itemStacks = new org.bukkit.inventory.ItemStack[4];
                itemStacks[0] = new org.bukkit.inventory.ItemStack(Material.DIAMOND_BOOTS);
                itemStacks[1] = new org.bukkit.inventory.ItemStack(Material.DIAMOND_LEGGINGS);
                itemStacks[2] = new org.bukkit.inventory.ItemStack(Material.DIAMOND_CHESTPLATE);
                itemStacks[3] = new ItemStack(Material.DIAMOND_HELMET);
                inventory.setArmorContents(itemStacks);
                // ������
                inventory.setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.BOW));
                inventory.setItemInOffHand(new org.bukkit.inventory.ItemStack(Material.SHIELD));
            }

        }
        // AI ÿ��20 �� tick
        BukkitTask bukkitTask = new AutoPlayNavigator(friday).runTaskTimer(PluginCore.getInstance(), 20, 10);

        return friday;
    }

    /**
     * ����ʵ��
     * @param friday
     * @return
     */
    public static void remove(Friday friday) {
        Player fridayPlayer = friday.getBukkitEntity();
        if (fridayPlayer != null) {
            fridayPlayer.removeMetadata(Friday.NPC_FLAG, PluginCore.getInstance());
            NMSUtil.removeFromWorld(fridayPlayer);
            NMSUtil.addOrRemoveFromPlayerList(fridayPlayer, true);
            NMSUtil.remove(fridayPlayer);

        }
        PluginCore.setFriday(null);
        logger.info("[Friday] Remove Friday!");
    }

    /**
     * ��ȡ��������Ʒ�������������
     * @param fridayPlayer
     * @return
     */
    public static ItemStack[] getInventory(Player fridayPlayer){
        if (fridayPlayer != null){
            ItemStack[] itemStacks = fridayPlayer.getInventory().getContents().clone();
            List<ItemStack> temp = new ArrayList<>();
            for (int i=9; i < 36; i++){
                if (itemStacks[i] != null){
                    temp.add(itemStacks[i]);
                }
            }
            return temp.isEmpty() ? null : temp.toArray(new ItemStack[temp.size()]);
        }
        return null;
    }
}
