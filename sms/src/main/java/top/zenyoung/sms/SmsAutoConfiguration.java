package top.zenyoung.sms;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.sms.config.SmsProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * 短信-自动配置
 *
 * @author young
 */
@Configuration
@EnableConfigurationProperties({SmsProperties.class})
public class SmsAutoConfiguration {
    @Bean
    public SmsServiceFactory getServiceFactory(@Nonnull final SmsProperties smsProperties,
                                               @Nullable final List<SmsUpCallbackListener> callbacks) {
        final SmsServiceFactory factory = SmsServiceFactoryDefault.of(smsProperties, callbacks);
        factory.init();
        return factory;
    }

    /**
     * 短信签名管理
     *
     * @param factory 短信管理工厂
     * @return 签名管理
     */
    @Bean
    public SmsSignManageService getSignManageService(@Nullable final SmsServiceFactory factory) {
        return Optional.ofNullable(factory)
                .map(SmsServiceFactory::getSignManageService)
                .orElse(null);
    }

    /**
     * 短信模板管理
     *
     * @param factory 短信管理工厂
     * @return 模板管理
     */
    @Bean
    public SmsTemplateManageService getTemplateManageService(@Nullable final SmsServiceFactory factory) {
        return Optional.ofNullable(factory)
                .map(SmsServiceFactory::getTemplateManageService)
                .orElse(null);
    }

    /**
     * 短信发送服务
     *
     * @param factory 短信管理工厂
     * @return 短信发送
     */
    @Bean
    public SmsSenderService getSenderService(@Nullable final SmsServiceFactory factory) {
        return Optional.ofNullable(factory)
                .map(SmsServiceFactory::getSenderService)
                .orElse(null);
    }

    /**
     * 短信发送统计服务
     *
     * @param factory 短信管理工厂
     * @return 发送统计
     */
    @Bean
    public SmsSenderStatisticsService getSenderStatisticsService(@Nullable final SmsServiceFactory factory) {
        return Optional.ofNullable(factory)
                .map(SmsServiceFactory::getSenderStatisticsService)
                .orElse(null);
    }
}
