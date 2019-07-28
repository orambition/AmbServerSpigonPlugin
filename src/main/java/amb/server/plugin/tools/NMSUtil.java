package amb.server.plugin.tools;

import amb.server.plugin.service.ap.entity.Friday;
import amb.server.plugin.service.ap.network.EmptyChannel;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * ����nms������
 */
public class NMSUtil {
    /**
     * ���� - ��ȡ����ָ�����Ƶ�����
     * @param clazz
     * @param field
     * @return
     */
    public static Field getField(Class<?> clazz, String field) {
        Field f = null;
        if (clazz != null){
            try {
                f = clazz.getDeclaredField(field);
                f.setAccessible(true);
            } catch (Exception e) {
                // ignored
            }
        }
        return f;
    }

    /**
     * ���� - java7�������ڷ��䷽�����ɼ��ٷ�����������
     */
    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * ���� - ���Ե�get����
     * @param clazz
     * @param name
     * @return
     */
    public static MethodHandle getGetter(Class<?> clazz, String name) {
        try {
            return LOOKUP.unreflectGetter(getField(clazz, name));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ���� - ���Ե�sat����
     * @param clazz
     * @param field
     * @return
     */
    public static MethodHandle getFinalSetter(Class<?> clazz, String field) {
        try {
            return LOOKUP.unreflectSetter(getField(clazz, field));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * ���� - ���ڲ����߼�����
     */
    private static final MethodHandle G = getGetter(AdvancementDataPlayer.class, "g");
    private static final MethodHandle H = getGetter(AdvancementDataPlayer.class, "h");
    private static final MethodHandle I = getGetter(AdvancementDataPlayer.class, "i");
    private static final MethodHandle ADVANCEMENT_PLAYER_FIELD = getFinalSetter(EntityPlayer.class,"advancementDataPlayer");
    /**
     * NMS - ���ø߼�����
     * @param entity
     * @param instance
     */
    public static void setAdvancement(Player entity, AdvancementDataPlayer instance) {
        try {
            ADVANCEMENT_PLAYER_FIELD.invoke(getHandle(entity), instance);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * NMS - ����߼�����
     * @param data
     */
    public static void clearAdvancementData(AdvancementDataPlayer data) {
        data.a();
        data.data.clear();
        try {
            ((Set<?>) G.invoke(data)).clear();
            ((Set<?>) H.invoke(data)).clear();
            ((Set<?>) I.invoke(data)).clear();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * NMS - ͨ��bukkitʵ���ȡnmsʵ��
     * @param entity
     * @return
     */
    public static net.minecraft.server.v1_14_R1.Entity getHandle(Entity entity) {
        if (!(entity instanceof CraftEntity)) {
            return null;
        }
        return ((CraftEntity) entity).getHandle();
    }

    /**
     * ����ҷ������ݰ�
     * @param player
     * @param packet
     */
    public static void  sendPacket(Player player, Packet<?> packet){
        if (packet != null){
            ((EntityPlayer)getHandle(player)).playerConnection.sendPacket(packet);
        }
    }

    /**
     * ��ʵ�帽���ĵ���ҷ������ݰ�
     * @param from
     * @param radius
     * @param packets
     */
    public static void sendPacketsNearby(Player from, double radius, Packet<?>... packets) {
        radius *= radius;
        Location location = from.getLocation();
        final World world = location.getWorld();
        for (Player ply : Bukkit.getServer().getOnlinePlayers()) {
            if (ply == null || world != ply.getWorld() || (from != null && !ply.canSee(from))) {
                continue;
            }
            if (location.distanceSquared(ply.getLocation()) > radius) {
                continue;
            }
            for (Packet<?> packet : packets) {
                sendPacket(ply, packet);
            }
        }
    }

    /**
     * ��ʼ�� �Զ����netty handle Ϊ empty
     * @param networkManager
     */
    public static void initNetworkManager(NetworkManager networkManager){
        networkManager.channel = new EmptyChannel(null);
        SocketAddress socketAddress = new SocketAddress() {
            private static final long serialVersionUID = 8207338859896320185L;
        };
        networkManager.socketAddress = socketAddress;
    }

    /**
     *
     * @param entity
     * @param spawnReason
     * @return
     */
    /**
     * ��ʵ���������� ����ʵ����ָ��λ��
     * @param entity
     * @param location
     * @return
     */
    public static boolean addEntityToWorld(Entity entity, Location location) {
        boolean result = getHandle(entity).getWorld().addEntity(getHandle(entity));
        if (result){
            setHeadYaw(entity, location.getYaw());
            entity.teleport(location);
        }
        return result;
    }

    /**
     * ����ҷ����Զ���ʵ�����Ϣ��ʹ�Զ���ʵ��ɼ�
     * @param recipient
     * @param listPlayer
     */
    public static void sendTabListAdd(Player recipient, Player listPlayer){
        EntityPlayer entityPlayer = ((CraftPlayer)listPlayer).getHandle();
        sendPacket(recipient, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
    }

    /**
     * ��ʵ����������������б���ɾ��/���
     * @param entity
     * @param remove
     */
    public static void addOrRemoveFromPlayerList(Entity entity, boolean remove){
        if (entity != null){
            EntityPlayer handel = (EntityPlayer)getHandle(entity);
            if (handel.getWorld() != null){
                if (remove){
                    handel.getWorld().getPlayers().remove(handel);
                } else if (!handel.getWorld().getPlayers().contains(handel)){
                    ((List)handel.getWorld().getPlayers()).add(handel);
                }
            }
        }
    }

    public static float getStepHeight(Entity entity){
        return getHandle(entity).K;
    }
    public static void setStepHeight(Entity entity, float height){
        getHandle(entity).K = height;
    }

    /**
     * ����ʵ��ͷ�ĽǶ�
     * @param entity
     * @param yaw
     */
    public static void setHeadYaw(Entity entity, float yaw){
        if (!(entity instanceof LivingEntity)){
            return;
        }
        EntityLiving handle = (EntityLiving) getHandle(entity);
        handle.aL = yaw;
        if (!(handle instanceof EntityHuman)){
            handle.aK = yaw;
        }
        handle.aM = yaw;
    }

    /**
     * һ��ʵ�忴����һ��
     * @param from
     * @param to
     */
    public static void look(Entity from, Entity to) {
        net.minecraft.server.v1_14_R1.Entity handle = NMSUtil.getHandle(from);
        net.minecraft.server.v1_14_R1.Entity target = NMSUtil.getHandle(to);
        if (handle instanceof Friday) {
            ((Friday) handle).setTargetLook(target, 10F, 40F);
        }
    }

    public static void setDestination(Entity entity, double x, double y, double z, float speed) {
        net.minecraft.server.v1_14_R1.Entity handle = NMSUtil.getHandle(entity);
        if (handle == null)
            return;
        if (handle instanceof Friday) {
            ((Friday) handle).setMoveDestination(x, y, z, speed);
        }
    }
}
