package amb.server.plugin.service.ap;

import amb.server.plugin.service.ap.entity.Friday;
import amb.server.plugin.tools.NMSUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AutoPlayService {

    public static Entity addAutoPlayer(Location location) {
        // 获取地址所处的nmsWorld
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        // 创建玩家资料
        GameProfile profile = new GameProfile(UUID.randomUUID(), "[AP]Mr_Amb");
        // 创建玩家交互管理器
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(nmsWorld);
        // 创建自定义nms实体
        Friday friday = new Friday(nmsWorld.getServer().getServer(), nmsWorld, profile, playerInteractManager);
        // 将nms实体 转换为bukkit实体
        Entity entity = friday.getBukkitEntity();
        // 将自定义实体添加到世界
        NMSUtil.addEntityToWorld(entity, location);

        // 向所在世界的所有(可见)玩家发送实体信息
        entity.getWorld().getPlayers().forEach(player ->{
            if (player.canSee((Player) entity) ){
                if (location.distanceSquared(player.getLocation()) < 100){
                    NMSUtil.sendTabListAdd(player, (Player) entity);
                }
            }
        });

        // 将自定义实体加入到所在世界的玩家列表
        NMSUtil.addOrRemoveFromPlayerList(entity, false);
        System.out.println("Add Friday to PlayerList:"+entity.getWorld().getPlayers().toString());

        // 设置实体不会离开视线消失
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.setRemoveWhenFarAway(false);
            if (NMSUtil.getStepHeight(livingEntity) < 1) {
                NMSUtil.setStepHeight(livingEntity, 1);
            }
        }

        return entity;
    }
}
