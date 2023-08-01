package top.zenyoung.sms;

import javax.annotation.Nonnull;

/**
 * 回调监听器基类
 *
 * @param <T> 监听数据类型
 * @author young
 */
public interface CallbackListener<T> {
    /**
     * 接收消息处理
     *
     * @param dto 接收消息
     */
    void receiveHandler(@Nonnull final T dto);
}
