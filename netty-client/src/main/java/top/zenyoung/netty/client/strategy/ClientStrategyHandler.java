package top.zenyoung.netty.client.strategy;

import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.strategy.StrategyHandler;

/**
 * 服务端策略处理器
 *
 * @author young
 */
public interface ClientStrategyHandler<M extends Message> extends StrategyHandler<M> {

}
