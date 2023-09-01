package top.zenyoung.sms.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信模板-新增VO
 *
 * @author yangyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsTemplateAddVO extends BaseSmsVO {
    /**
     * 模板Code
     */
    private String templateCode;
}
