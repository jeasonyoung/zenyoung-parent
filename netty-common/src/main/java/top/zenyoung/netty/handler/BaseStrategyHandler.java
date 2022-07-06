package top.zenyoung.netty.handler;

import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;

/**
 * 策略处理器基接口
 *
 * @author young
 */
public interface BaseStrategyHandler<T extends Message> {
    /**
     * 获取命令名称
     *
     * @return 命令名称
     */
    String[] getCommands();

    /**
     * 是否支持处理消息
     *
     * @param req 消息数据
     * @return 是否支持处理
     */
    default boolean supported(@Nonnull final T req) {
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
     * @param req     请求数据
     * @return 响应数据
     */
    T process(@Nonnull final Session session, @Nonnull final T req);
}
