package amb.server.plugin.service.aip.network;

import io.netty.channel.*;

import java.net.SocketAddress;

/**
 * ¼Ì³Ð×Ô netty channl
 */
public class EmptyChannel extends AbstractChannel {
    private final ChannelConfig channelConfig = new DefaultChannelConfig(this);

    public EmptyChannel(Channel parent) {
        super(parent);
    }

    public ChannelConfig config() {
        channelConfig.setAutoRead(true);
        return channelConfig;
    }

    protected AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected boolean isCompatible(EventLoop eventLoop) {
        return true;
    }

    protected SocketAddress localAddress0() {
        return null;
    }

    protected SocketAddress remoteAddress0() {
        return null;
    }

    protected void doBind(SocketAddress socketAddress) throws Exception {

    }

    protected void doDisconnect() throws Exception {

    }

    protected void doClose() throws Exception {

    }

    protected void doBeginRead() throws Exception {

    }

    protected void doWrite(ChannelOutboundBuffer channelOutboundBuffer) throws Exception {

    }

    public boolean isOpen() {
        return false;
    }

    public boolean isActive() {
        return false;
    }

    public ChannelMetadata metadata() {
        return new ChannelMetadata(true);
    }
}
