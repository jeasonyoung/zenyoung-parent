package com.huawei.services.runtime;

/**
 * 上下文接口
 *
 * @author young
 */
public interface Context {

    /**
     * 获取函数剩余运行时间
     *
     * @return 函数剩余运行时间
     */
    int getRemainingTimeInMilliSeconds();

    /**
     * 获取用户委托的AccessKey(有效期24小时),使用该方法需要给函数配置委托
     *
     * @return AccessKey
     */
    String getAccessKey();

    /**
     * 获取用户委托的SecretKey(有效期24小时),使用该方法需要给函数配置委托。
     *
     * @return SecretKey
     */
    String getSecretKey();

    /**
     * 获取安全AccessKey
     *
     * @return 安全AccessKey
     */
    String getSecurityAccessKey();

    /**
     * 获取安全SecretKey
     *
     * @return 安全SecretKey
     */
    String getSecuritySecretKey();

    /**
     * 通过key获取用户通过环境变量传入的值
     *
     * @return 环境变量传入的值
     */
    String getUserData(final String key);

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    String getFunctionName();

    /**
     * 获取函数超时时间
     *
     * @return 函数超时时间
     */
    int getRunningTimeInSeconds();

    /**
     * 获取函数的版本
     *
     * @return 函数的版本
     */
    String getVersion();

    /**
     * 获取分配的内存
     *
     * @return 分配的内存
     */
    int getMemorySize();

    /**
     * 获取函数组
     *
     * @return 函数组
     */
    String getPackage();

    /**
     * 获取用户委托的token(有效期24小时),使用该方法需要给函数配置委托
     *
     * @return 用户委托的token
     */
    String getToken();

    /**
     * 获取安全Token
     *
     * @return 安全Token
     */
    String getSecurityToken();

    /**
     * 获取context提供的logger方法(默认会输出时间、请求ID等信息)
     *
     * @return context提供的logger方法
     */
    RuntimeLogger getLogger();

    /**
     * 获取函数的别名
     *
     * @return 函数的别名
     */
    String getAlias();
}
