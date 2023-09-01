package top.zenyoung.sms.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信签名-新增VO
 *
 * @author yangyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsSignAddVO extends BaseSmsVO {
    /**
     * 签名名称
     */
    private String signName;
}
