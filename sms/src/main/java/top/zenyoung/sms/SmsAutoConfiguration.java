package top.zenyoung.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.sms.config.SmsProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 短信-自动配置
 *
 * @author young
 */
@Configuration
@EnableConfigurationProperties({SmsProperties.class})
public class SmsAutoConfiguration implements CommandLineRunner {
    private final AtomicReference<SmsServiceFactory> refFactory = new AtomicReference<>(null);

    @Override
    public void run(final String... args) {
        final SmsServiceFactory factory = refFactory.get();
        if (Objects.nonNull(factory)) {
            factory.init();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public SmsServiceFactory getServiceFactory(@Nonnull final SmsProperties prop,
                                               @Nonnull final ObjectMapper objMapper,
                                               @Nullable final List<SmsUpCallbackListener> smsUpCallbacks,
                                               @Nullable final List<SmsReportCallbackListener> smsReportCallbacks) {
        if (!Strings.isNullOrEmpty(prop.getType()) && !Strings.isNullOrEmpty(prop.getAppKey())) {
            final SmsServiceFactory factory = new SmsServiceFactoryDefault(prop, objMapper, smsUpCallbacks, smsReportCallbacks);
            refFactory.set(factory);
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
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
    public SmsSenderStatisticsService getSenderStatisticsService(@Nonnull final SmsServiceFactory factory) {
        return factory.getSenderStatisticsService();
    }
}
