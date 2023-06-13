package top.zenyoung.netty.handler;

import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;

/**
 * 策略处理器基类
 *
 * @author young
 */
public abstract class BaseStrategyHandler<T extends Message> {
    /**
     * 获取命令名称
     *
     * @return 命令名称
     */
    @Nonnull
    public abstract String[] getCommands();

    /**
     * 是否支持处理消息
     *
     * @param req 消息数据
     * @return 是否支持处理
     */
    public boolean supported(@Nonnull final T req) {
        return true;
    }

    /**
     * 获取策略优先级
     *
     * @return 优先级
     */
    public int priority() {
        return 0;
    }

    /**
     * 业务处理
     *
     * @param session 当前会话用户
     * @param req     请求数据
     * @return 响应数据
     */
    public abstract T process(@Nonnull final Session session, @Nonnull final T req);
}
