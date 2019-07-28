package amb.server.plugin.service.ap.network;

import amb.server.plugin.tools.NMSUtil;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_14_R1.EnumProtocolDirection;
import net.minecraft.server.v1_14_R1.NetworkManager;
import net.minecraft.server.v1_14_R1.Packet;

/**
 * ¼Ì³Ð×Ônetty handle
 */
public class EmptyNetworkManager extends NetworkManager {
    public EmptyNetworkManager(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        NMSUtil.initNetworkManager(this);
    }
    @Override
    public boolean isConnected(){
        return true;
    }

    @Override
    public void sendPacket(Packet packet, GenericFutureListener genericFutureListener){

    }
}
