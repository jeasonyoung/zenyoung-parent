package top.zenyoung.netty.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import top.zenyoung.netty.client.config.NettyClientProperties;
import top.zenyoung.netty.client.strategy.ClientStrategyHandlerFactory;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.event.IdleStateEvent;
import top.zenyoung.netty.handler.BaseSocketHandler;
import top.zenyoung.netty.session.Session;
import top.zenyoung.netty.strategy.StrategyHandlerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Socket客户端-业务处理接口实现
 *
 * @author young
 */
@Slf4j
public abstract class BaseClientSocketHandler<T extends Message> extends BaseSocketHandler<T> implements ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext context) throws BeansException {
        this.context = context;
    }

    protected void contextHandler(@Nonnull final Consumer<ApplicationContext> handler) {
        Optional.ofNullable(context).ifPresent(handler);
    }

    protected <R> R getContextBean(@Nonnull final Class<R> cls) {
        return Optional.ofNullable(context)
                .map(c -> c.getBean(cls))
                .orElse(null);
    }

    /**
     * 获取客户端配置
     *
     * @return 服务配置
     */
    protected abstract NettyClientProperties getProperties();

    @Override
    protected Integer getHeartbeatTimeoutTotal() {
        return Optional.ofNullable(getProperties())
                .map(NettyClientProperties::getHeartbeatTimeoutTotal)
                .filter(total -> total > 0)
                .orElse(3);
    }

    protected BaseClientSocketHandler() {
        this.ensureHasScope();
    }

    @Override
    protected void heartbeatIdleHandle(@Nonnull final ChannelHandlerContext ctx,
                                       @Nullable final Session session, @Nonnull final IdleState state) {
        contextHandler(c -> c.publishEvent(IdleStateEvent.of(session, state)));
    }

    @Override
    protected StrategyHandlerFactory getStrategyHandlerFactory() {
        return getContextBean(ClientStrategyHandlerFactory.class);
    }
}
