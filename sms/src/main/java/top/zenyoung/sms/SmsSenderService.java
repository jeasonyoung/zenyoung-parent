package top.zenyoung.sms;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.springframework.validation.annotation.Validated;
import top.zenyoung.sms.dto.SmsSenderDTO;
import top.zenyoung.sms.exption.SmsException;
import top.zenyoung.sms.vo.SmsSenderVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 短信发送-服务接口
 *
 * @author yangyong
 */
public interface SmsSenderService {
    /**
     * 短信发送(含验证码)
     *
     * @param template 短信模板
     * @param code     验证码参数
     * @param mobile   手机号
     * @return 发送结果
     * @throws SmsException 异常
     */
    default SmsSenderVO sender(@Nonnull final String template, @Nonnull final String code, @Nonnull final String mobile) throws SmsException {
        final Map<String, Object> data = Maps.newHashMap();
        data.put("code", code);
        return sender(template, data, null, mobile);
    }

    /**
     * 短信发送(含批量)
     *
     * @param template 短信模板
     * @param data     参数集合
     * @param signName 签名
     * @param mobiles  手机号码集合
     * @return 发送结果
     * @throws SmsException 异常
     */
    default SmsSenderVO sender(@Nonnull final String template, @Nonnull final Map<String, Object> data,
                               @Nullable final String signName, @Nonnull final String... mobiles) throws SmsException {
        final SmsSenderDTO dto = new SmsSenderDTO();
        //模板
        dto.setTemplate(template);
        //模板参数
        dto.setData(data);
        //签名
        dto.setSignName(signName);
        //接收人手机(多个手机号可用,分隔,最大不超过100个)
        dto.setMobile(Joiner.on(",").skipNulls().join(mobiles));
        //发送短信
        return sender(dto);
    }

    /**
     * 短信发送(可上行)
     *
     * @param dto 发送参数
     * @return 发送结果
     * @throws SmsException 发送异常
     */
    SmsSenderVO sender(@Validated @Nonnull final SmsSenderDTO dto) throws SmsException;

    /**
     * 批量短信发送(可上行)
     *
     * @param dtos 发送参数集合
     * @return 发送结果
     * @throws SmsException 发送异常
     */
    SmsSenderVO sender(@Valid @Nonnull final List<SmsSenderDTO> dtos) throws SmsException;
}
