package top.zenyoung.netty.handler;

import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;

/**
 * 策略工厂接口
 *
 * @author young
 */
public interface StrategyFactory {
    /**
     * 策略任务处理
     *
     * @param session 当前会话
     * @param req     请求数据
     * @param <T>     消息数据类型
     * @return 响应数据
     */
    <T extends Message> T process(@Nonnull final Session session, @Nonnull final T req);
}
