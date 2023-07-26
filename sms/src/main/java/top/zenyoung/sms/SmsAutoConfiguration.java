package top.zenyoung.sms;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.sms.aliyun.AliSmsServiceFactory;
import top.zenyoung.sms.config.SmsProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
        final SmsServiceFactory factory = AliSmsServiceFactory.of(smsProperties, callbacks);
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
    public SmsSignManageService getSignManageService(@Nonnull final SmsServiceFactory factory) {
        return factory.getSignManageService();
    }

    /**
     * 短信模板管理
     *
     * @param factory 短信管理工厂
     * @return 模板管理
     */
    @Bean
    public SmsTemplateManageService getTemplateManageService(@Nonnull final SmsServiceFactory factory) {
        return factory.getTemplateManageService();
    }

    /**
     * 短信发送服务
     *
     * @param factory 短信管理工厂
     * @return 短信发送
     */
    @Bean
    public SmsSenderService getSenderService(@Nonnull final SmsServiceFactory factory) {
        return factory.getSenderService();
    }

    /**
     * 短信发送统计服务
     *
     * @param factory 短信管理工厂
     * @return 发送统计
     */
    @Bean
    public SmsSenderStatisticsService getSenderStatisticsService(@Nonnull final SmsServiceFactory factory) {
        return factory.getSenderStatisticsService();
    }
}
