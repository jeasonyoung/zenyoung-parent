package top.zenyoung.netty.strategy;

import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * 策略处理器工厂
 *
 * @author young
 */
@FunctionalInterface
public interface StrategyHandlerFactory {
    /**
     * 策略处理器
     *
     * @param session         会话
     * @param req             请求数据
     * @param callbackHandler 回调处理
     */
    <M extends Message> void process(@Nonnull final Session session, @Nonnull final M req, @Nonnull final Consumer<M> callbackHandler);
}
