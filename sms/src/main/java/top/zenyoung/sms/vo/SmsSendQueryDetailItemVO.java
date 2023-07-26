package top.zenyoung.sms.vo;

import lombok.Data;
import top.zenyoung.sms.enums.SmsSendStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信发送详情-查询项目VO
 *
 * @author yangyong
 */
@Data
public class SmsSendQueryDetailItemVO implements Serializable {
    /**
     * 运营商短信状态码
     */
    private String errCode;
    /**
     * 模板Code
     */
    private String templateCode;
    /**
     * 外部流水扩展字段
     */
    private String outId;
    /**
     * 短信接收时间
     */
    private Date receiveDate;
    /**
     * 短信发送时间
     */
    private Date sendDate;
    /**
     * 接收短信手机号码
     */
    private String mobile;
    /**
     * 短信内容
     */
    private String content;
    /**
     * 短信发送状态
     */
    private SmsSendStatus status;
}
