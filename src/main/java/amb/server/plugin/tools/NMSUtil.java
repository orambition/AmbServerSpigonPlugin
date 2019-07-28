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
 * 操作nms工具类
 */
public class NMSUtil {
    /**
     * 反射 - 获取类中指定名称的属性
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
     * 反射 - java7特性用于反射方法，可减少反射的冗余代码
     */
    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * 反射 - 属性的get方法
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
     * 反射 - 属性的sat方法
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
     * 反射 - 用于操作高级数据
     */
    private static final MethodHandle G = getGetter(AdvancementDataPlayer.class, "g");
    private static final MethodHandle H = getGetter(AdvancementDataPlayer.class, "h");
    private static final MethodHandle I = getGetter(AdvancementDataPlayer.class, "i");
    private static final MethodHandle ADVANCEMENT_PLAYER_FIELD = getFinalSetter(EntityPlayer.class,"advancementDataPlayer");
    /**
     * NMS - 设置高级数据
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
     * NMS - 清除高级数据
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
     * NMS - 通过bukkit实体获取nms实体
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
     * 向玩家发送数据包
     * @param player
     * @param packet
     */
    public static void  sendPacket(Player player, Packet<?> packet){
        if (packet != null){
            ((EntityPlayer)getHandle(player)).playerConnection.sendPacket(packet);
        }
    }

    /**
     * 向实体附近的的玩家发送数据包
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
     * 初始化 自定义的netty handle 为 empty
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
     * 向实体所在世界 生成实体在指定位置
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
     * 向玩家发送自定义实体的信息，使自定义实体可见
     * @param recipient
     * @param listPlayer
     */
    public static void sendTabListAdd(Player recipient, Player listPlayer){
        EntityPlayer entityPlayer = ((CraftPlayer)listPlayer).getHandle();
        sendPacket(recipient, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
    }

    /**
     * 将实体从所在世界的玩家列表中删除/添加
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
     * 设置实体头的角度
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
     * 一个实体看向另一个
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
