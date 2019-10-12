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
     * ��������ɾ��ʵ��
     * @param entity
     */
    public static void removeFromWorld(Entity entity) {
        Preconditions.checkNotNull(entity);
        ((WorldServer)getHandle(entity).world).removeEntity(getHandle(entity));
    }
    /**
     * ����ҷ����Զ���ʵ�����Ϣ��ʹ�Զ���ʵ��ɼ�
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
                    //handel.getWorldServer().getServer().getHandle().players.remove(handel);
                } else if (!handel.getWorld().getPlayers().contains(handel)){
                    ((List)handel.getWorld().getPlayers()).add(handel);
                    //handel.getWorldServer().getServer().getHandle().players.add(handel);
                }
            }
        }
    }

    /**
     * ����ʵ��
     * @param entity
     */
    public static void remove(Entity entity) {
        entity.remove();
        getHandle(entity).die();
    }
    /**
     * ִ��ָ������
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
                // Ĭ�϶���
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
                // �ʳ�
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
            ((Friday) handle).setTargetLook(target, 100F, 90F);
        }
    }

    /**
     * ����ʵ��
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
     * ����ʵ��
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
     * ������Χʵ��
     * @param radius
     * @return
     */
    public static List<Entity> foundNearbyHostile(Entity player, double radius) {
        // ���˹���ʵ��
        Predicate<Entity> attackEntity = e -> (e instanceof Monster && e.getType() != EntityType.PIG_ZOMBIE)
                || e.getType() == EntityType.SLIME || e.getType() == EntityType.MAGMA_CUBE
                || e.getType() == EntityType.PHANTOM;
        Collection<Entity> nearbyEntity = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, attackEntity);
        return nearbyEntity.stream().sorted(Comparator.comparing(e -> player.getLocation().distance(e.getLocation()))).collect(Collectors.toList());

    }
}
