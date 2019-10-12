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
     * 创建一个 ai实体
     * @param location
     * @return
     */
    public static Friday addAutoPlayer(String name, UUID uuid, Location location) {
        // 获取地址所处的nmsWorld
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        // 创建玩家资料
        GameProfile profile = new GameProfile(uuid, name);
        // 创建玩家交互管理器
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(nmsWorld);
        // 创建自定义nms实体
        Friday friday = new Friday(nmsWorld.getServer().getServer(), nmsWorld, profile, playerInteractManager);

        // 通过此方法可添加一个真实玩家，替换Mr_Amb可以用此方法
        //nmsWorld.getServer().getHandle().a(friday.playerConnection.networkManager, friday);

        // 将nms实体 转换为bukkit实体
        Player playerFriday = friday.getBukkitEntity();
        // 将自定义实体添加到世界
        boolean addEntitySuccess = NMSUtil.addEntityToWorld(playerFriday, location);
        if (addEntitySuccess){
            // 向所在世界的所有(可见)玩家发送实体信息
            playerFriday.getWorld().getPlayers().forEach(player -> NMSUtil.sendTabListAdd(player, playerFriday));
            // 将自定义实体加入到所在世界的玩家列表，不然游戏逻辑不生效，如：怪物攻击
            NMSUtil.addOrRemoveFromPlayerList(playerFriday, false);
            logger.info("[Friday] Add Friday at " + playerFriday.getLocation().toString());
            return friday;
        } else {
            logger.info("[Friday] Add Friday Failed ");
        }

        return null;
    }

    /**
     * 初始化 ai 实体
     * @param friday
     * @return
     */
    public static Friday initAutoPlayer(Friday friday, Player player){
        // 初始化装备
        if (friday != null) {
            PlayerInventory inventory = friday.getBukkitEntity().getInventory();
            ItemStack itemStack = new ItemStack(Material.BARRIER);
            for (int i=0; i < 9; i++){
                inventory.setItem(i, itemStack);
            }
            if (player != null){
                // 衣服
                inventory.setArmorContents(player.getInventory().getArmorContents());
                // 主副手
                inventory.setItemInMainHand(player.getInventory().getItemInMainHand());
                inventory.setItemInOffHand(player.getInventory().getItemInOffHand());
            } else {
                // 衣服
                org.bukkit.inventory.ItemStack[] itemStacks = new org.bukkit.inventory.ItemStack[4];
                itemStacks[0] = new org.bukkit.inventory.ItemStack(Material.DIAMOND_BOOTS);
                itemStacks[1] = new org.bukkit.inventory.ItemStack(Material.DIAMOND_LEGGINGS);
                itemStacks[2] = new org.bukkit.inventory.ItemStack(Material.DIAMOND_CHESTPLATE);
                itemStacks[3] = new ItemStack(Material.DIAMOND_HELMET);
                inventory.setArmorContents(itemStacks);
                // 主副手
                inventory.setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.BOW));
                inventory.setItemInOffHand(new org.bukkit.inventory.ItemStack(Material.SHIELD));
            }

        }
        // AI 每秒20 个 tick
        BukkitTask bukkitTask = new AutoPlayNavigator(friday).runTaskTimer(PluginCore.getInstance(), 20, 10);

        return friday;
    }

    /**
     * 销毁实体
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
     * 获取背包的物品，不包含快捷栏
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
