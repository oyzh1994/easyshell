package cn.oyzh.easyshell.vnc;

import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.proxy.ProxyHandler;
import org.jfxvnc.net.rfb.VncConnection;
import org.jfxvnc.net.rfb.codec.ProtocolInitializer;

/**
 * @author oyzh
 * @since 2025-08-22
 */
public class ShellVNCConnection extends VncConnection {

    private ShellProxyConfig proxyConfig;

    public void setProxyConfig(ShellProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    public ShellProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    @Override
    protected ChannelHandler initHandler() {
        if (this.proxyConfig == null) {
            return super.initHandler();
        }
        return new ProtocolInitializer(super.getRender(), super.getConfig()) {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                ProxyHandler proxyHandler = ShellProxyUtil.initProxy3(proxyConfig);
                pipeline.addLast(proxyHandler);
                super.initChannel(ch);
            }
        };
    }
}
