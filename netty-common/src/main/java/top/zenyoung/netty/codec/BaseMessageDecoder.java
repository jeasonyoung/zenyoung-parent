package top.zenyoung.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Optional;

/**
 * 消息解码器
 *
 * @author young
 */
public abstract class BaseMessageDecoder<T extends Message> extends ChannelInboundHandlerAdapter implements MessageDecoder<T> {
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        Optional.of(msg)
                .map(this::decoder)
                .ifPresent(ctx::fireChannelRead);
    }
}
