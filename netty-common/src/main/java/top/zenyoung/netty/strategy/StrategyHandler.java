package top.zenyoung.netty.strategy;

import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;

/**
 * 策略处理器
 *
 * @author young
 */
public interface StrategyHandler {
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
    boolean supported(@Nonnull final Message req);

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
    Message process(@Nonnull final Session session, @Nonnull final Message req);
}
