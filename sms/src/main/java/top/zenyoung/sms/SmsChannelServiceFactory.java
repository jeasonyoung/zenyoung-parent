package top.zenyoung.sms;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.sms.config.SmsChannelProperty;
import top.zenyoung.sms.service.SmsChannelService;
import top.zenyoung.sms.service.impl.SmsChannelAliServiceImpl;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 短信通道-服务工厂类
 *
 * @author young
 */
@Slf4j
public class SmsChannelServiceFactory {

    /**
     * 创建短信通道服务
     *
     * @param property 配置属性
     * @return 短信通道服务
     */
    public static SmsChannelService create(@Nullable final SmsChannelProperty property) {
        if (Objects.nonNull(property)) {
            try {
                final String type = property.getType();
                final ChannelType channel = Strings.isNullOrEmpty(type) ? ChannelType.Ali : ChannelType.valueOf(type);
                if (channel == ChannelType.Ali) {
                    return new SmsChannelAliServiceImpl(property.getAppKey(), property.getSecret());
                }
            } catch (Throwable e) {
                log.error("create(property: {})[type: {}]-exp: {}", property, property.getType(), e.getMessage());
            }
        }
        return null;
    }

    /**
     * 短信通道类型
     */
    private enum ChannelType {
        /**
         * 阿里云
         */
        Ali
    }
}
