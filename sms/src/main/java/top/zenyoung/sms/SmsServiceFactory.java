package top.zenyoung.sms;

/**
 * 短信-服务工厂接口
 *
 * @author yangyong
 */
public interface SmsServiceFactory {
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
