package top.zenyoung.framework.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证令牌
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthToken extends UserInfo {
    /**
     * 授权令牌
     */
    private String token;
    /**
     * 刷新令牌
     */
    private String refershToken;
}
