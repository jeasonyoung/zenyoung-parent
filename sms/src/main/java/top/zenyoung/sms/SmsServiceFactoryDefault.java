package top.zenyoung.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.zenyoung.sms.aliyun.AliSmsServiceFactory;
import top.zenyoung.sms.config.SmsProperties;

import javax.annotation.Nonnull;
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
     * @param objMapper ObjectMapper
     */
    public SmsServiceFactoryDefault(@Nonnull final ObjectMapper objMapper) {
        this.factory = new AliSmsServiceFactory(objMapper);
    }

    @Override
    public void setSmsProps(@Nonnull final SmsProperties smsProps) {
        this.factory.setSmsProps(smsProps);
    }

    @Override
    public void setSmsUpCallbacks(@Nonnull final List<SmsUpCallbackListener> smsUpCallbacks) {
        this.factory.setSmsUpCallbacks(smsUpCallbacks);
    }

    @Override
    public void setSmsReportCallbacks(@Nonnull final List<SmsReportCallbackListener> smsReportCallbacks) {
        this.factory.setSmsReportCallbacks(smsReportCallbacks);
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
