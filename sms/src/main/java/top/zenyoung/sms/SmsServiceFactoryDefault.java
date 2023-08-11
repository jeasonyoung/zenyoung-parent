package top.zenyoung.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.zenyoung.sms.aliyun.AliSmsServiceFactory;
import top.zenyoung.sms.config.SmsProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 短信-服务工厂默认实现
 *
 * @author young
 */
public class SmsServiceFactoryDefault implements SmsServiceFactory {
    private final SmsServiceFactory factory;

    /**
     * 构造函数
     *
     * @param props              短信通道配置
     * @param objMapper          ObjectMapper
     * @param smsUpCallbacks     上行短信回调
     * @param smsReportCallbacks 短信发送状态报告
     */
    public SmsServiceFactoryDefault(@Nonnull final SmsProperties props, @Nonnull final ObjectMapper objMapper,
                                    @Nullable final List<SmsUpCallbackListener> smsUpCallbacks,
                                    @Nullable final List<SmsReportCallbackListener> smsReportCallbacks) {
        this.factory = new AliSmsServiceFactory(props, objMapper, smsUpCallbacks, smsReportCallbacks);
    }

    @Override
    public void init() {
        this.factory.init();
    }

    @Override
    public SmsSenderService getSenderService() {
        return this.factory.getSenderService();
    }

    @Override
    public SmsSenderStatisticsService getSenderStatisticsService() {
        return this.factory.getSenderStatisticsService();
    }

    @Override
    public SmsSignManageService getSignManageService() {
        return this.factory.getSignManageService();
    }

    @Override
    public SmsTemplateManageService getTemplateManageService() {
        return this.factory.getTemplateManageService();
    }
}
