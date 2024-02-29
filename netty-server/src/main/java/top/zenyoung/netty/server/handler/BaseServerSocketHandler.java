package top.zenyoung.netty.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.event.IdleStateEvent;
import top.zenyoung.netty.handler.BaseSocketHandler;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.strategy.ServerStrategyHandlerFactory;
import top.zenyoung.netty.session.Session;
import top.zenyoung.netty.strategy.StrategyHandlerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Socket服务端-业务处理接口实现
 *
 * @author young
 */
@Slf4j
public abstract class BaseServerSocketHandler<M extends Message> extends BaseSocketHandler<M> implements ApplicationContextAware {
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
     * 获取服务配置
     *
     * @return 服务配置
     */
    protected abstract NettyServerProperties getProperties();

    @Override
    protected Integer getHeartbeatTimeoutTotal() {
        return Optional.ofNullable(getProperties())
                .map(NettyServerProperties::getHeartbeatTimeoutTotal)
                .orElse(null);
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
        contextHandler(c -> c.publishEvent(IdleStateEvent.of(session, state)));
    }

    @Override
    protected StrategyHandlerFactory getStrategyHandlerFactory() {
        return getContextBean(ServerStrategyHandlerFactory.class);
    }

    /**
     * 检查是否在黑名单中
     *
     * @param session Session会话
     * @return 是否存在
     */
    protected boolean checkBlackList(@Nonnull final Session session) {
        return Optional.ofNullable(getProperties())
                .map(p -> p.checkBlackList(session.getClientIp()))
                .orElse(false);
    }

    /**
     * 添加到黑名单
     *
     * @param session Session会话
     * @return 添加结果
     */
    protected boolean addBlackList(@Nonnull final Session session) {
        return Optional.ofNullable(getProperties())
                .map(p -> p.addBlackList(session.getClientIp()))
                .orElse(false);
    }

    /**
     * 从黑名单中移除
     *
     * @param session Session会话
     * @return 移除结果
     */
    protected boolean removeBlackList(@Nonnull final Session session) {
        return Optional.ofNullable(getProperties())
                .map(p -> p.removeBlackList(session.getClientIp()))
                .orElse(false);
    }
}
