package top.zenyoung.netty.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.config.RequestLimit;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求限制过滤器
 *
 * @author young
 */
@Slf4j
public class RequestLimitFilter extends ChannelInboundHandlerAdapter {
    private final AtomicLong refCount = new AtomicLong(0L);
    private final RequestLimit limit;

    private ScheduledFuture<?> future;

    /**
     * 构造函数
     *
     * @param properties 配置属性
     */
    public RequestLimitFilter(@Nonnull final NettyServerProperties properties) {
        this.limit = properties.getLimit();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        this.init(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        this.destroy();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (Objects.nonNull(limit) && Objects.nonNull(this.future)) {
            final long count = this.refCount.incrementAndGet();
            final long max = Math.max(this.limit.getMax(), 0);
            if (max > 0 && count > max) {
                final Channel channel = ctx.channel();
                final InetSocketAddress clientIpAddr = (InetSocketAddress) channel.remoteAddress();
                final long sec = limit.getDelay().getSeconds();
                ctx.close();
                throw new IllegalStateException(clientIpAddr + ",通道[" + channel + "," + sec + "]秒内请求次数(" + count + ")过多,关闭通道,停止访问!");
            }
            if (count >= Long.MAX_VALUE) {
                this.refCount.set(0L);
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (Objects.nonNull(cause)) {
            log.warn(cause.getMessage());
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        this.destroy();
        super.handlerRemoved(ctx);
    }

    private void init(@Nonnull final ChannelHandlerContext ctx) {
        final Duration delay;
        if (Objects.nonNull(this.limit) && Objects.nonNull(delay = this.limit.getDelay())) {
            this.future = this.schedule(ctx, () -> this.refCount.set(0L), delay);
        }
    }

    private void destroy() {
        if (Objects.nonNull(this.future)) {
            this.future.cancel(false);
            this.future = null;
        }
    }

    private ScheduledFuture<?> schedule(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Runnable task, @Nonnull final Duration delay) {
        return ctx.executor().schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }
}
