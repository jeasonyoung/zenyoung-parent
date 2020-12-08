package top.zenyoung.wechat.common;

import lombok.Getter;
import top.zenyoung.common.model.EnumValue;

/**
 * 授权类型-枚举
 * @author yangyong
 * @version 1.0
 * date 2020/7/12 1:33 下午
 **/
@Getter
public enum GrantType implements EnumValue {
    /**
     * client_credential
     */
    ClientCredential(0, "client_credential"),
    /**
     * authorization_code
     */
    AuthorizationCode(1, "authorization_code"),
    /**
     * refresh_token
     */
    RefreshToken(2, "refresh_token");

    private final int val;
    private final String title;

    GrantType(final int val, final String title){
        this.val = val;
        this.title = title;
    }
}
