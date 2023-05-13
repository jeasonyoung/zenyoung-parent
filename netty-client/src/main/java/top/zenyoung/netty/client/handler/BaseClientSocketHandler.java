package top.zenyoung.netty.client.handler;

import io.netty.channel.ChannelHandlerContext;
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
import java.util.Optional;

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
        return Optional.ofNullable(properites)
                .map(NettyClientProperties::getHeartbeatTimeoutTotal)
                .filter(total -> total > 0)
                .orElse(3);
    }

    @Override
    protected StrategyFactory getStrategyFactory() {
        return Optional.ofNullable(this.strategyFactory)
                .orElseThrow(() -> new IllegalArgumentException("未加载到'clientStrategyFactory'策略处理工厂"));
    }

    @Override
    protected void heartbeatIdleHandle(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Session session, @Nonnull final IdleState state) {
        super.heartbeatIdleHandle(ctx, session, state);
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
