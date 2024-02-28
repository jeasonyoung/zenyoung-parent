package top.zenyoung.netty.strategy;

import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;

/**
 * 策略处理器
 *
 * @author young
 */
public interface StrategyHandler<M extends Message> {
    /**
     * 获取命令名称
     *
     * @return 命令名称
     */
    String[] getCommands();

    /**
     * 是否支持处理消息
     *
     * @param session 会话
     * @param data    消息数据
     * @return 是否支持处理
     */
    default boolean supported(@Nonnull final Session session, @Nonnull final M data) {
        return true;
    }

    /**
     * 获取策略优先级
     *
     * @return 优先级
     */
    default int priority() {
        return 0;
    }

    /**
     * 业务处理
     *
     * @param session 当前会话用户
     * @param data    消息数据
     * @return 响应数据
     */
    M process(@Nonnull final Session session, @Nonnull final M data);
}
