package amb.server.plugin.tools;

import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.service.aip.network.EmptyChannel;
import com.google.common.base.Preconditions;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
     * 从世界中删除实体
     * @param entity
     */
    public static void removeFromWorld(Entity entity) {
        Preconditions.checkNotNull(entity);
        ((WorldServer)getHandle(entity).world).removeEntity(getHandle(entity));
    }
    /**
     * 向玩家发送自定义实体的信息，使自定义实体可见
     * @param recipient
     * @param listPlayer
     */
    public static void sendTabListAdd(Player recipient, Player listPlayer){
        if (recipient != null && listPlayer != null){
            EntityPlayer entityPlayer = ((CraftPlayer)listPlayer).getHandle();
            sendPacket(recipient, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
        }
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
                    //handel.getWorldServer().getServer().getHandle().players.remove(handel);
                } else if (!handel.getWorld().getPlayers().contains(handel)){
                    ((List)handel.getWorld().getPlayers()).add(handel);
                    //handel.getWorldServer().getServer().getHandle().players.add(handel);
                }
            }
        }
    }

    /**
     * 销毁实体
     * @param entity
     */
    public static void remove(Entity entity) {
        entity.remove();
        getHandle(entity).die();
    }
    /**
     * 执行指定动作
     * @param animation
     * @param player
     * @param radius
     */
    public static void playAnimation(PlayerAnimation animation, Player player, int radius) {
        final EntityPlayer entityPlayer = (EntityPlayer) getHandle(player);
        switch (animation) {
            case ARM_SWING:
            case HURT:
            case EAT_FOOD:
            case ARM_SWING_OFFHAND:
            case CRIT:
            case MAGIC_CRIT:
                // 默认动作
                PacketPlayOutAnimation packet = new PacketPlayOutAnimation(entityPlayer, animation.getCode());
                sendPacketsNearby(player, radius, packet);
                break;
            case SIT:
            case STOP_SITTING:
            case SLEEP:
            case STOP_SLEEPING:
                throw new UnsupportedOperationException(); // TODO
            case SNEAK:
                player.setSneaking(true);
                PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true);
                sendPacketsNearby(player, radius, metadataPacket);
                break;
            case STOP_SNEAKING:
                player.setSneaking(false);
                PacketPlayOutEntityMetadata metadataPacket1 = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true);
                sendPacketsNearby(player, radius, metadataPacket1);
                break;
            case START_ELYTRA:
                // 鞘翅
                entityPlayer.J();
                break;
            case START_USE_MAINHAND_ITEM:
                entityPlayer.c(EnumHand.MAIN_HAND);
                PacketPlayOutEntityMetadata metadataPacket2 = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true);
                sendPacketsNearby(player, radius, metadataPacket2);
                break;
            case START_USE_OFFHAND_ITEM:
                entityPlayer.c(EnumHand.OFF_HAND);
                PacketPlayOutEntityMetadata metadataPacket3 = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true);
                sendPacketsNearby(player, radius, metadataPacket3);
                break;
            case STOP_USE_ITEM:
                entityPlayer.clearActiveItem();
                PacketPlayOutEntityMetadata metadataPacket4 = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true);
                sendPacketsNearby(player, radius, metadataPacket4);
                break;
            default:
                throw new UnsupportedOperationException();
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
            ((Friday) handle).setTargetLook(target, 100F, 90F);
        }
    }

    /**
     * 攻击实体
     * @param player
     * @param entity
     * @param haveBow
     */
    public static void attickEntity(Player player, Entity entity, boolean haveBow, double attickRange){
        if (entity instanceof LivingEntity &&!entity.isDead()) {
            look(player, entity);
            Location eyeLocation = player.getEyeLocation().clone();
            Location targetLocation = ((LivingEntity) entity).getEyeLocation();
            int distance = (int) eyeLocation.distance(targetLocation);
            if (distance <= attickRange){
                PlayerAnimation.ARM_SWING.play(player, 64);
                ((LivingEntity) entity).damage(5);
                knockback(player, (LivingEntity) entity);
            } else if (distance < 16 && haveBow){
                PlayerAnimation.START_USE_OFFHAND_ITEM.play(player, 64);
                float speed = 2f;
                Vector relative = targetLocation.toVector().subtract(eyeLocation.toVector()).normalize();
                relative.setY(relative.getY()+distance/(speed*speed*40));

                Arrow arrow = player.getWorld().spawnArrow(eyeLocation, relative, speed,0f);
                arrow.setShooter(player);
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                arrow.setDamage(10D);
                arrow.setCritical(true);
                arrow.setPierceLevel(12);
            }
        }
    }
    /**
     * 击退实体
     * @param entity
     */
    public static void knockback(Player player, LivingEntity entity) {
        Vector relative = entity.getLocation().toVector().subtract(player.getLocation().toVector());
        if (relative.lengthSquared() > 0) {
            relative = relative.normalize();
        }
        relative.setY(0.75);
        relative.multiply(0.5 / Math.max(1.0, entity.getVelocity().length()));
        entity.setVelocity(entity.getVelocity().multiply(0.5).add(relative));
    }

    /**
     * 搜索周围实体
     * @param radius
     * @return
     */
    public static List<Entity> foundNearbyHostile(Entity player, double radius) {
        // 过滤攻击实体
        Predicate<Entity> attackEntity = e -> (e instanceof Monster && e.getType() != EntityType.PIG_ZOMBIE)
                || e.getType() == EntityType.SLIME || e.getType() == EntityType.MAGMA_CUBE
                || e.getType() == EntityType.PHANTOM;
        Collection<Entity> nearbyEntity = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, attackEntity);
        return nearbyEntity.stream().sorted(Comparator.comparing(e -> player.getLocation().distance(e.getLocation()))).collect(Collectors.toList());

    }
}
