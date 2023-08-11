package top.zenyoung.sms;

import top.zenyoung.sms.config.SmsProperties;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 短信-服务工厂接口
 *
 * @author yangyong
 */
public interface SmsServiceFactory {
    /**
     * 设置短信配置
     *
     * @param smsProps 短信配置
     */
    void setSmsProps(@Nonnull final SmsProperties smsProps);

    /**
     * 设置短信上行回调集合
     *
     * @param smsUpCallbacks 上行回调集合
     */
    void setSmsUpCallbacks(@Nonnull final List<SmsUpCallbackListener> smsUpCallbacks);

    /**
     * 设置短信发送状态回调集合
     *
     * @param smsReportCallbacks 发送状态回调集合
     */
    void setSmsReportCallbacks(@Nonnull final List<SmsReportCallbackListener> smsReportCallbacks);

    /**
     * 初始化
     */
    void init();

    /**
     * 获取短信发送服务
     *
     * @return 发送服务
     */
    SmsSenderService getSenderService();

    /**
     * 获取短信发送统计服务
     *
     * @return 发送统计服务
     */
    SmsSenderStatisticsService getSenderStatisticsService();

    /**
     * 获取签名管理服务
     *
     * @return 签名管理服务
     */
    SmsSignManageService getSignManageService();

    /**
     * 获取模板管理服务
     *
     * @return 模板管理服务
     */
    SmsTemplateManageService getTemplateManageService();
}
