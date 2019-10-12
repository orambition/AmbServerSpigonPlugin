package amb.server.plugin.service.aip.entity;

import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.aip.entity.controller.PlayerControllerJump;
import amb.server.plugin.service.aip.entity.controller.PlayerControllerLook;
import amb.server.plugin.service.aip.entity.controller.PlayerControllerMove;
import amb.server.plugin.service.aip.network.EmptyNetHandler;
import amb.server.plugin.service.aip.network.EmptyNetworkManager;
import amb.server.plugin.tools.NMSUtil;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginLogger;

import java.util.Map;
import java.util.logging.Logger;

/**
 * �Զ����NMSʵ��
 */
public class Friday extends EntityPlayer {
    private static final Logger LOGGER = PluginLogger.getLogger("Friday");
    public static final String NPC_FLAG = "AMB`s FRIDAY";
    private final int PACKET_UPDATE_DELAY = 30;
    /* ·������ */
    private FridayNavigation navigation;
    private final Map<PathType, Float> bz = Maps.newEnumMap(PathType.class);

    private PlayerControllerJump controllerJump;
    private PlayerControllerLook controllerLook;
    private PlayerControllerMove controllerMove;

    private int updateCounter = 0;
    private int jumpTicks = 0;

    /** AutoPlay ������� **/
    private PlayMode playMode = PlayMode.FREE;
    private Location guardsLocation; // ��λλ��
    private Player followTarget; // �������
    private org.bukkit.entity.Entity damageTarget; // ����ʵ�幥��

    public Friday(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        initialise();
    }

    /**
     * ��ʼ��ʵ��
     */
    private void initialise() {
        // ����һ�� ���������(netty InboundHandler)
        NetworkManager conn = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
        // ��Ҫ ���� playerConnection����Ȼ���п�ָ��
        this.playerConnection = new EmptyNetHandler(this.server, conn, this);

        this.controllerJump = new PlayerControllerJump(this);
        this.controllerLook = new PlayerControllerLook(this);
        this.controllerMove = new PlayerControllerMove(this);

        // �����Զ���ʵ�����Ϸģʽ
        this.playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);

        // ����Ѱ·����
        this.navigation = new FridayNavigation(this, world);

        org.bukkit.entity.Entity entity = getBukkitEntity();
        // ����ʵ�岻���뿪������ʧ
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.setRemoveWhenFarAway(false);
            //livingEntity.setCanPickupItems(true);
            if (NMSUtil.getStepHeight(livingEntity) < 1) {
                // ���� ÿ��Ѱ·�������˵ľ���
                NMSUtil.setStepHeight(livingEntity, 1);
            }
        }
        entity.setMetadata(NPC_FLAG, new FixedMetadataValue(PluginCore.getInstance(), true));
    }

    /**
     * ��ȡʵ�� �Դ�·�����͵�Ȩ�أ�
     *
     * @param pathtype
     * @return
     */
    public float a(PathType pathtype) {
        return this.bz.containsKey(pathtype) ? this.bz.get(pathtype).floatValue() : pathtype.a();
    }

    /**
     * ����ʵ��� ��·�����͵�Ȩ��
     *
     * @param pathtype
     * @param f
     */
    public void a(PathType pathtype, float f) {
        this.bz.put(pathtype, Float.valueOf(f));
    }

    /**
     * ����ʵ�� �� ��Ŀ��
     *
     * @param target
     * @param yawOffset
     * @param renderOffset
     */
    public void setTargetLook(Entity target, float yawOffset, float renderOffset) {
        controllerLook.a(target, yawOffset, renderOffset);
    }
    public void setTargetLook(Location target) {
        controllerLook.a(target.getBlockX(), target.getBlockY(), target.getBlockZ(), 90f, 80f);
    }

    /**
     * ÿtickִ��
     */
    @Override
    public void tick() {
        super.tick();
        // ����ʵ�������Ϊ���������������˵�Ч��
        this.entityBaseTick();
        this.playerTick();
        //this.movementTick();
        //updatePackets(false);

        // ����δ���
        if (!navigation.n()) {
            // ִ�е���
            navigation.c();
        }

        // �����ƶ���Ϊ������Ŀ�꣬���� '�ٶ�����'
        controllerMove.a();
        controllerLook.a();
        controllerJump.b();
        // ����������˶���Ϊ������ '�ٶ�����' �ƶ�
        moveOnCurrentHeading();
    }

    /**
     * ���ݷ��������ƶ�
     */
    private void moveOnCurrentHeading() {
        if (jumping) {
            if (onGround && jumpTicks == 0) {
                jump();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }
        bb *= 0.98F;
        bd *= 0.98F;
        be *= 0.9F;
        // �˶������� ����Ϊ�������ٶȵ�������
        e(new Vec3D(this.bb, this.bc, this.bd));
        NMSUtil.setHeadYaw(getBukkitEntity(), yaw);
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        f = 1;
        boolean damaged = super.damageEntity(damagesource, f);
        if (damagesource instanceof EntityDamageSource) {
            Entity entity = damagesource.getEntity();
            if (entity instanceof EntityArrow) {
                EntityArrow entityarrow = (EntityArrow)entity;
                damageTarget = entityarrow.getShooter().getBukkitEntity();
            } else {
                damageTarget = entity.getBukkitEntity();
            }
            if (damageTarget.equals(followTarget)){
                damageTarget = null;
            }
        }
        return damaged;
    }

    private void updatePackets(boolean navigating) {
        if (updateCounter++ <= PACKET_UPDATE_DELAY)
            return;

        updateCounter = 0;
        Packet<?>[] packets = new Packet[navigating ? EnumItemSlot.values().length : EnumItemSlot.values().length + 1];
        if (!navigating) {
            packets[5] = new PacketPlayOutEntityHeadRotation(this,
                    (byte) MathHelper.d(this.getHeadRotation() * 256.0F / 360.0F));
        }
        int i = 0;
        for (EnumItemSlot slot : EnumItemSlot.values()) {
            packets[i++] = new PacketPlayOutEntityEquipment(getId(), slot, getEquipment(slot));
        }
        NMSUtil.sendPacketsNearby(getBukkitEntity(), 64, packets);
    }

    public FridayNavigation getNavigation() {
        return navigation;
    }

    public PlayerControllerJump getControllerJump() {
        return controllerJump;
    }

    public PlayerControllerLook getControllerLook() {
        return controllerLook;
    }

    public PlayerControllerMove getControllerMove() {
        return controllerMove;
    }

    public PlayMode getPlayMode() {
        return playMode;
    }

    public void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    public Location getGuardsLocation() {
        return guardsLocation;
    }

    public void setGuardsLocation(Location guardsLocation) {
        this.guardsLocation = guardsLocation;
    }

    public Player getFollowTarget() {
        return followTarget;
    }

    public void setFollowTarget(Player followTarget) {
        this.followTarget = followTarget;
    }

    public org.bukkit.entity.Entity getDamageTarget() {
        return damageTarget;
    }

    public org.bukkit.entity.Entity setDamageTarget(org.bukkit.entity.Entity damageTarget) {
        org.bukkit.entity.Entity temp = this.damageTarget;
        this.damageTarget = damageTarget;
        return temp;
    }

    public enum PlayMode{
        FREE,
        FOLLOW,
        GUARDS,
        ;
    }
}
