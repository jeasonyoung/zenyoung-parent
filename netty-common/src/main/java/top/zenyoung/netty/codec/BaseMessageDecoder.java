package top.zenyoung.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.Optional;

/**
 * 消息解码器
 *
 * @author young
 */
public abstract class BaseMessageDecoder<T extends Message> extends ByteToMessageDecoder implements MessageDecoder<T> {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        Optional.ofNullable(decoder(in))
                .ifPresent(out::add);
    }
}
