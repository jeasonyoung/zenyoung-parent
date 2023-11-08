package top.zenyoung.boot.annotation;

/**
 * 限流类型
 *
 * @author young
 */
public enum LimitPolicy {
    /**
     * 全局限流
     */
    GLOBAL,
    /**
     * 按IP限流
     */
    IP,
    /**
     * 按用户限流
     */
    USER
}
