package top.zenyoung.boot.constant;

import java.io.Serializable;

/**
 * 应用常量
 *
 * @author young
 */
public interface HeaderConstants extends Serializable {
    /**
     * Token前缀
     */
    String BEARER_PREFIX = "Bearer ";
    /**
     * 全局唯一ID
     */
    String TO_REQ_ID = "tranceId";
    /**
     * 请求时间戳
     */
    String TO_REQ_TIME = "begTime";
    /**
     * 账号ID
     */
    String ACCOUNT_ID = "accountId";
}
