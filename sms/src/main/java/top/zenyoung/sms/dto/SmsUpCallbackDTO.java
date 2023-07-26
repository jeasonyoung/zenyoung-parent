package top.zenyoung.sms.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 上行短信消息
 *
 * @author yangyong
 */
@Data
@RequiredArgsConstructor(staticName = "of")
public class SmsUpCallbackDTO implements Serializable {
    /**
     * 短信扩展号码
     */
    private final String destCode;
    /**
     * 短信发送时间
     */
    private final Date sendTime;
    /**
     * 消息序列ID
     */
    private final String sequenceId;
    /**
     * 短信接收号码
     */
    private final String mobile;
    /**
     * 短信内容
     */
    private final String content;
}
