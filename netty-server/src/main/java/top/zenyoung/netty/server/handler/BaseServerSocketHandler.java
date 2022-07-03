package top.zenyoung.netty.server.handler;

import io.netty.handler.timeout.IdleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.BaseSocketHandler;
import top.zenyoung.netty.handler.StrategyFactory;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.event.ChannelIdleStateEvent;
import top.zenyoung.netty.server.session.ChannelSessionMap;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;

/**
 * Socket服务端-业务处理接口实现
 *
 * @author young
 */
@Slf4j
public abstract class BaseServerSocketHandler<T extends Message> extends BaseSocketHandler<T> {

    @Autowired
    private NettyServerProperties properties;

    @Autowired
    @Qualifier("serverStrategyFactory")
    private StrategyFactory strategyFactory;

    @Override
    protected Integer getHeartbeatTimeoutTotal() {
        return this.properties.getHeartbeatTimeoutTotal();
    }

    @Override
    protected StrategyFactory getStrategyFactory() {
        return this.strategyFactory;
    }

    public BaseServerSocketHandler() {
        this.ensureHasScope();
    }

    @Override
    protected final void heartbeatIdleHandle(@Nonnull final Session session, @Nonnull final IdleState state) {
        final ChannelIdleStateEvent event = new ChannelIdleStateEvent();
        event.setSession(session);
        event.setState(state);
        this.publishContextEvent(event);
    }

    @Override
    protected void buildSessionAfter(@Nonnull final Session session) {
        ChannelSessionMap.put(session);
    }

    @Override
    protected void close(@Nonnull final Session session) {
        ChannelSessionMap.remove(session);
    }
}
