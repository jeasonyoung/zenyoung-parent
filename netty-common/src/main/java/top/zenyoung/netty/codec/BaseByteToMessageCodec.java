package top.zenyoung.netty.codec;

import io.netty.handler.codec.ByteToMessageCodec;

/**
 * 消息编解码器基类(byte<=>message)
 *
 * @param <T> 消息类型
 * @author young
 */
public abstract class BaseByteToMessageCodec<T extends Message> extends ByteToMessageCodec<T> {

}
