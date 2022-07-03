package top.zenyoung.netty.client.handler;

import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.BaseStrategyHandler;

/**
 * 客户端策略处理接口
 *
 * @author young
 */
public interface ClientStrategyHandler<T extends Message> extends BaseStrategyHandler<T> {

}