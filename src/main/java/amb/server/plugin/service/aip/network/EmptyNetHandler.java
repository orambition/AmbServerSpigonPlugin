package amb.server.plugin.service.aip.network;

import net.minecraft.server.v1_14_R1.*;

/**
 * playerConnection用于向玩家发送数据
 */
public class EmptyNetHandler extends PlayerConnection {

    public EmptyNetHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet<?> packet){

    }
}
