package top.zenyoung.sms;

import lombok.RequiredArgsConstructor;
import top.zenyoung.sms.aliyun.AliSmsServiceFactory;
import top.zenyoung.sms.config.SmsProperties;

import java.util.List;
import java.util.Optional;

/**
 * 短信-服务工厂接口-默认实现
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class SmsServiceFactoryDefault implements SmsServiceFactory {
    private final SmsProperties smsProperties;
    private final List<SmsUpCallbackListener> callbacks;

    private SmsServiceFactory factory;

    @Override
    public void init() {
        factory = AliSmsServiceFactory.of(smsProperties, callbacks);
        factory.init();
    }

    @Override
    public SmsSenderService getSenderService() {
        return Optional.ofNullable(factory)
                .map(SmsServiceFactory::getSenderService)
                .orElse(null);
    }

    @Override
    public SmsSenderStatisticsService getSenderStatisticsService() {
        return Optional.ofNullable(factory)
                .map(SmsServiceFactory::getSenderStatisticsService)
                .orElse(null);
    }

    @Override
    public SmsSignManageService getSignManageService() {
        return Optional.ofNullable(factory)
                .map(SmsServiceFactory::getSignManageService)
                .orElse(null);
    }

    @Override
    public SmsTemplateManageService getTemplateManageService() {
        return Optional.ofNullable(factory)
                .map(SmsServiceFactory::getTemplateManageService)
                .orElse(null);
    }
}
