package top.zenyoung.netty.codec;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息编码器基类(message=>byte)
 *
 * @author young
 */
@ChannelHandler.Sharable
public abstract class BaseMessageToByteEncoder<T extends Message> extends MessageToByteEncoder<T> {

}
