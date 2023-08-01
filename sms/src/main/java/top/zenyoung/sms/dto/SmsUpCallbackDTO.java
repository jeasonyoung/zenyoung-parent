package top.zenyoung.sms.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 上行短信消息
 *
 * @author young
 */
@Data
public class SmsUpCallbackDTO implements Serializable {
    /**
     * 短信扩展号码
     */
    @JsonAlias({"dest_code"})
    private String destCode;
    /**
     * 短信发送时间
     */
    @JsonAlias({"send_time"})
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;
    /**
     * 消息序列ID
     */
    @JsonAlias({"sequence_id"})
    private String sequenceId;
    /**
     * 短信接收号码
     */
    @JsonAlias({"phone_number"})
    private String mobile;
    /**
     * 短信内容
     */
    @JsonAlias({"content"})
    private String content;
}
