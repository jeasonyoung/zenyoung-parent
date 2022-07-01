package top.zenyoung.netty.handler;

import io.netty.channel.ChannelInboundHandler;

/**
 * Socket业务处理接口
 *
 * @author young
 */
public interface SocketHandler extends ChannelInboundHandler {
    /**
     * SpringBean Scope
     */
    String SCOPE_PROTOTYPE = "prototype";
}
