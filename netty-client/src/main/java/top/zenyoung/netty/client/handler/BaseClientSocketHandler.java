package top.zenyoung.netty.client.handler;

import io.netty.handler.timeout.IdleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import top.zenyoung.netty.client.config.NettyClientProperties;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.event.IdleStateEvent;
import top.zenyoung.netty.handler.BaseSocketHandler;
import top.zenyoung.netty.handler.StrategyFactory;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;

/**
 * Socket客户端-业务处理接口实现
 *
 * @author young
 */
@Slf4j
public class BaseClientSocketHandler<T extends Message> extends BaseSocketHandler<T> {
    /**
     * 注入客户端配置
     */
    @Autowired
    private NettyClientProperties properites;

    @Autowired
    @Qualifier("clientStrategyFactory")
    private StrategyFactory strategyFactory;

    @Override
    protected Integer getHeartbeatTimeoutTotal() {
        return properites.getHeartbeatTimeoutTotal();
    }

    @Override
    protected StrategyFactory getStrategyFactory() {
        return this.strategyFactory;
    }

    @Override
    protected void heartbeatIdleHandle(@Nonnull final Session session, @Nonnull final IdleState state) {
        final IdleStateEvent event = new IdleStateEvent();
        event.setState(state);
        this.publishContextEvent(event);
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
