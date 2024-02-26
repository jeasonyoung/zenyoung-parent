package top.zenyoung.netty.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.BaseSocketHandler;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.event.ChannelIdleStateEvent;
import top.zenyoung.netty.server.strategy.ServerStrategyHandlerFactory;
import top.zenyoung.netty.session.Session;
import top.zenyoung.netty.strategy.StrategyHandlerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    private ApplicationContext context;

    @Override
    protected Integer getHeartbeatTimeoutTotal() {
        return this.properties.getHeartbeatTimeoutTotal();
    }

    protected BaseServerSocketHandler() {
        this.ensureHasScope();
    }

    /**
     * 支持端口
     *
     * @param port 端口
     */
    public boolean supportedPort(final int port) {
        return true;
    }

    @Override
    protected void heartbeatIdleHandle(@Nonnull final ChannelHandlerContext ctx,
                                       @Nullable final Session session, @Nonnull final IdleState state) {
        final ChannelIdleStateEvent event = new ChannelIdleStateEvent();
        event.setSession(session);
        event.setState(state);
        context.publishEvent(event);
    }

    @Override
    protected StrategyHandlerFactory getStrategyHandlerFactory() {
        return context.getBean(ServerStrategyHandlerFactory.class);
    }

    /**
     * 检查是否在黑名单中
     *
     * @param session Session会话
     * @return 是否存在
     */
    protected boolean checkBlackList(@Nonnull final Session session) {
        return this.properties.checkBlackList(session.getClientIp());
    }

    /**
     * 添加到黑名单
     *
     * @param session Session会话
     * @return 添加结果
     */
    protected boolean addBlackList(@Nonnull final Session session) {
        return this.properties.addBlackList(session.getClientIp());
    }

    /**
     * 从黑名单中移除
     *
     * @param session Session会话
     * @return 移除结果
     */
    protected boolean removeBlackList(@Nonnull final Session session) {
        return this.properties.removeBlackList(session.getClientIp());
    }
}
