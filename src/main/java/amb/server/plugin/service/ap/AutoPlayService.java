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
        // ��ȡ��ַ������nmsWorld
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        // �����������
        GameProfile profile = new GameProfile(UUID.randomUUID(), "[AP]Mr_Amb");
        // ������ҽ���������
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(nmsWorld);
        // �����Զ���nmsʵ��
        Friday friday = new Friday(nmsWorld.getServer().getServer(), nmsWorld, profile, playerInteractManager);
        // ��nmsʵ�� ת��Ϊbukkitʵ��
        Entity entity = friday.getBukkitEntity();
        // ���Զ���ʵ����ӵ�����
        NMSUtil.addEntityToWorld(entity, location);

        // ���������������(�ɼ�)��ҷ���ʵ����Ϣ
        entity.getWorld().getPlayers().forEach(player ->{
            if (player.canSee((Player) entity) ){
                if (location.distanceSquared(player.getLocation()) < 100){
                    NMSUtil.sendTabListAdd(player, (Player) entity);
                }
            }
        });

        // ���Զ���ʵ����뵽�������������б�
        NMSUtil.addOrRemoveFromPlayerList(entity, false);
        System.out.println("Add Friday to PlayerList:"+entity.getWorld().getPlayers().toString());

        // ����ʵ�岻���뿪������ʧ
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
