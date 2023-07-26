package top.zenyoung.sms;

import top.zenyoung.sms.dto.SmsUpCallbackDTO;

import javax.annotation.Nonnull;

/**
 * 短信回调-监听接口
 *
 * @author yangyong
 */
public interface SmsUpCallbackListener {

    /**
     * 接收消息处理
     *
     * @param dto 接收消息
     */
    void receiveHandler(@Nonnull final SmsUpCallbackDTO dto);
}
