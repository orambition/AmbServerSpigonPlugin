package amb.server.plugin.service.ap.network;

import net.minecraft.server.v1_14_R1.*;

/**
 * playerConnection��������ҷ�������
 */
public class EmptyNetHandler extends PlayerConnection {

    public EmptyNetHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet<?> packet){

    }
}
