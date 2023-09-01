package top.zenyoung.sms.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信发送VO
 *
 * @author yangyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsSenderVO extends BaseSmsVO {
    /**
     * 回执ID
     */
    private String bizId;
}
