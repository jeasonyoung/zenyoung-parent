package top.zenyoung.netty.codec;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 消息编码接口
 *
 * @author young
 */
@ChannelHandler.Sharable
public abstract class BaseMessageEncoder<T extends Message> extends ChannelOutboundHandlerAdapter {

    @Override
    @SuppressWarnings({"unchecked"})
    public final void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof Message) {
            final T in = (T) msg;
            encode(ctx, in, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    /**
     * 编码处理
     *
     * @param ctx     通道上下文
     * @param in      编码消息
     * @param promise 通道promise
     */
    protected void encode(@Nonnull final ChannelHandlerContext ctx, @Nonnull final T in, @Nullable final ChannelPromise promise) {
        final Object out = encode(ctx.alloc(), in);
        if (Objects.nonNull(out)) {
            ctx.write(out, promise);
        }
    }

    /**
     * 消息编码
     *
     * @param allocator 内存分配器
     * @param message   消息数据
     * @return 编码后的字节
     */
    protected abstract Object encode(@Nonnull final ByteBufAllocator allocator, @Nonnull final T message);
}
