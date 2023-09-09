package top.zenyoung.retrofit.retry;

/**
 * 触发重试的规则-枚举
 *
 * @author young
 */
public enum RetryRule {
    /**
     * 响应状态码不是2xx
     */
    RES_STATUS_NOT_2XX,
    /**
     * 发生任意异常
     */
    OCCUR_EXP,
    /**
     * 发生IO异常
     */
    OCCUR_IO_EXP,
}
