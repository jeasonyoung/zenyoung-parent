package top.zenyoung.netty.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import top.zenyoung.netty.client.config.NettyClientProperties;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.event.IdleStateEvent;
import top.zenyoung.netty.handler.BaseSocketHandler;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Socket客户端-业务处理接口实现
 *
 * @author young
 */
@Slf4j
public abstract class BaseClientSocketHandler<T extends Message> extends BaseSocketHandler<T> {

    @Autowired
    private volatile ApplicationContext context;

    @Override
    protected Integer getHeartbeatTimeoutTotal() {
        return Optional.of(context.getBean(NettyClientProperties.class))
                .map(NettyClientProperties::getHeartbeatTimeoutTotal)
                .filter(total -> total > 0)
                .orElse(3);
    }

    public BaseClientSocketHandler() {
        this.ensureHasScope();
    }

    @Override
    public final void handlerAdded(final ChannelHandlerContext ctx) {
        this.addCodec(ctx);
    }

    @Override
    public final void channelActive(final ChannelHandlerContext ctx) {
        if (!ctx.channel().config().isAutoClose()) {
            ctx.read();
        }
    }

    /**
     * 增加编解码器
     *
     * @param ctx 上下文
     */
    protected abstract void addCodec(@Nonnull final ChannelHandlerContext ctx);

    @Override
    protected void heartbeatIdleHandle(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Session session, @Nonnull final IdleState state) {
        final IdleStateEvent event = new IdleStateEvent();
        event.setState(state);
        context.publishEvent(event);
    }

    @Override
    protected void close(@Nonnull final Session session) {
        try {
            session.close();
        } catch (Throwable e) {
            log.warn("close(session: {})-exp: {}", session, e.getMessage());
        }
    }
}
