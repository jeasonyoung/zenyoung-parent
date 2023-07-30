package top.zenyoung.sms;

import com.google.common.base.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @ConditionalOnMissingBean
    public SmsServiceFactory getServiceFactory(@Nonnull final SmsProperties prop,
                                               @Nullable final List<SmsUpCallbackListener> callbacks) {
        if (!Strings.isNullOrEmpty(prop.getType()) && !Strings.isNullOrEmpty(prop.getAppKey())) {
            final SmsServiceFactory factory = SmsServiceFactoryDefault.of(prop, callbacks);
            factory.init();
            return factory;
        }
        return null;
    }

    /**
     * 短信签名管理
     *
     * @param factory 短信管理工厂
     * @return 签名管理
     */
    @Bean
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
    public SmsSenderStatisticsService getSenderStatisticsService(@Nullable final SmsServiceFactory factory) {
        return Optional.ofNullable(factory)
                .map(SmsServiceFactory::getSenderStatisticsService)
                .orElse(null);
    }
}
