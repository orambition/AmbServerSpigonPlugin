package amb.server.plugin.service.ap.entity;

import amb.server.plugin.service.ap.entity.controller.PlayerControllerJump;
import amb.server.plugin.service.ap.entity.controller.PlayerControllerLook;
import amb.server.plugin.service.ap.entity.controller.PlayerControllerMove;
import amb.server.plugin.service.ap.network.EmptyNetHandler;
import amb.server.plugin.service.ap.network.EmptyNetworkManager;
import amb.server.plugin.tools.NMSUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * �Զ����NMSʵ��
 */
public class Friday extends EntityPlayer {
    private final int PACKET_UPDATE_DELAY = 30;
    /**
     * ·������
     */
    private FridayNavigation navigation;
    private PlayerControllerJump controllerJump;
    private PlayerControllerLook controllerLook;
    private PlayerControllerMove controllerMove;

    private int updateCounter = 0;
    private int jumpTicks = 0;

    public Friday(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        initialise(minecraftserver);
        // �����Զ���ʵ�����Ϸģʽ
        playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);
    }

    /**
     * ��ʼ��ʵ��
     * @param minecraftServer
     */
    private void initialise(MinecraftServer minecraftServer){
        // ����һ�� ���������(netty InboundHandler)
        NetworkManager conn = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
        // ���� playerConnection ��Ҫ����Ȼ���п�ָ��
        playerConnection = new EmptyNetHandler(minecraftServer, conn, this);

        controllerJump = new PlayerControllerJump(this);
        controllerLook = new PlayerControllerLook(this);
        controllerMove = new PlayerControllerMove(this);

        // ����Ѱ·����
        navigation = new FridayNavigation(this, world);
        //NMSUtil.setStepHeight(getBukkitEntity(), 1);
        //NMSUtil.clearAdvancementData(this.getAdvancementData());
        //NMSUtil.setAdvancement(this.getBukkitEntity(), new EmptyAdvancementDataPlayer(minecraftServer, PluginCore.getInstance().getDataFolder().getParentFile(), this));
    }

    /**
     * ����ʵ�� �� ��Ŀ��
     * @param target
     * @param yawOffset
     * @param renderOffset
     */
    public void setTargetLook(Entity target, float yawOffset, float renderOffset) {
        controllerLook.a(target, yawOffset, renderOffset);
    }
    public void setMoveDestination(double x, double y, double z, double speed) {
        controllerMove.a(x, y, z, speed);
    }
    /**
     * ÿtickִ��
     */
    @Override
    public void tick(){
        super.tick();

        livingEntityBaseTick();

        updatePackets(false);

        // ����δ���
        if (!navigation.n()){
            // ִ�е���
            navigation.c();
        }
        moveOnCurrentHeading();


        controllerMove.a();
        controllerLook.a();
        controllerJump.b();
        Player taget = Bukkit.getPlayer("Mr_Amb");
        NMSUtil.look(getBukkitEntity(), taget);
        Location location = taget.getLocation();
        NMSUtil.setDestination(getBukkitEntity(), location.getX(), location.getBlockY(), location.getZ(), 1F);

    }

    public void livingEntityBaseTick() {
        entityBaseTick();
        this.aB = this.aC;
        if (this.hurtTicks > 0) {
            this.hurtTicks -= 1;
        }
        tickPotionEffects();
        this.aW = this.aV;
        this.aL = this.aK;
        this.aN = this.aM;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
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
        e(new Vec3D(this.bb, this.bc, this.bd)); // movement method
        NMSUtil.setHeadYaw(getBukkitEntity(), yaw);
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    /**
     * ���Լ���ӵ������ϵ�ָ��λ��
     * �����齫nms����д��NMSUtil����ĵط����ᵼ������ʱ�޸��߼�������
     * @param location
     */
    public void addSelfToWorld(Location location){
        this.world.addEntity(this);
        this.getBukkitEntity().teleport(location);
        this.aL = location.getYaw();
        this.aK = location.getYaw();
        this.aM = location.getYaw();
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
}
