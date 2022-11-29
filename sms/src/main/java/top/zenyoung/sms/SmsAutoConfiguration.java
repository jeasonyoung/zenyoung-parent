package top.zenyoung.sms;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.sms.config.SmsChannelProperty;
import top.zenyoung.sms.service.SmsChannelService;

/**
 * 短信-自动配置
 *
 * @author young
 */
@Configuration
@EnableConfigurationProperties({SmsChannelProperty.class})
public class SmsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SmsChannelService build(final ObjectProvider<SmsChannelProperty> channelProperties) {
        return SmsChannelServiceFactory.create(channelProperties.getIfAvailable());
    }
}
