package top.zenyoung.sms.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * 短信通道-服务接口
 *
 * @author young
 */
public interface SmsChannelService {

    /**
     * 发送短信
     *
     * @param templateCode 短信模板
     * @param params       模板参数集合
     * @param signName     短信签名
     * @param mobile       目标手机号码
     */
    void send(@Nonnull final String templateCode, @Nonnull final Map<String, Object> params, @Nullable final String signName, @Nonnull final String... mobile);
}
