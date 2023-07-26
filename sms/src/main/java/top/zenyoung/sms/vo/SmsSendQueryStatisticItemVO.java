package top.zenyoung.sms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信发送统计-查询项目ID
 *
 * @author yangyong
 */
@Data
public class SmsSendQueryStatisticItemVO implements Serializable {
    /**
     * 发送成功的短信条数
     */
    private Long totalCount;
    /**
     * 接收到回执成功的短信条数
     */
    private Long respondedSuccessCount;
    /**
     * 接收到回执失败的短信条数
     */
    private Long respondedFailCount;
    /**
     * 未收到回执的短信条数
     */
    private Long noRespondedCount;
    /**
     * 发送日期
     */
    private Date sendDate;
}
