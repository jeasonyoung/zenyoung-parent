package top.zenyoung.sms.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信发送状态报告
 *
 * @author young
 */
@Data
public class SmsReportCallbackDTO implements Serializable {
    /**
     * 发送时间
     */
    @JsonAlias({"send_time"})
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;
    /**
     * 回执时间
     */
    @JsonAlias({"report_time"})
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reportTime;
    /**
     * 短信接收手机号码
     */
    @JsonAlias({"phone_number"})
    private String mobile;
    /**
     * 短信长度(长短信拆分)
     */
    @JsonAlias({"sms_size"})
    private Integer size;
    /**
     * 短信回执ID
     */
    @JsonAlias({"biz_id"})
    private String bizId;
    /**
     * 外部流水ID
     */
    @JsonAlias({"out_id"})
    private String outId;
    /**
     * 是否发送成功
     */
    @JsonAlias({"success"})
    private Boolean success;
    /**
     * 错误码
     */
    @JsonAlias({"err_code"})
    private String errCode;
    /**
     * 错误码描述
     */
    @JsonAlias({"err_msg"})
    private String errMsg;
}
