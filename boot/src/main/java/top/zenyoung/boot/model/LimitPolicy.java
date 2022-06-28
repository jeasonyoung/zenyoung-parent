package top.zenyoung.boot.model;

/**
 * 限流类型
 *
 * @author young
 */
public enum LimitPolicy {
    /**
     * 全局限流
     */
    Global,
    /**
     * 按IP限流
     */
    IP,
    /**
     * 按用户限流
     */
    User
}
