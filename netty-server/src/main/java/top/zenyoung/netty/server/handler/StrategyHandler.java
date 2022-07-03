package top.zenyoung.netty.server.handler;

import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.BaseStrategyHandler;

/**
 * 策略处理接口
 *
 * @author young
 */
public interface StrategyHandler<T extends Message> extends BaseStrategyHandler<T> {

}
