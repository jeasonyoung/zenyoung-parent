package top.zenyoung.security.spi.token;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 令牌认证
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:28 下午
 **/
@Getter
@EqualsAndHashCode(callSuper = true)
public class TokenAuthentication extends UsernamePasswordAuthenticationToken {
    /**
     * 用户类型
     */
    private final Integer type;

    /**
     * 构造函数
     *
     * @param principal   认证账号
     * @param credentials 认证密码
     * @param type        用户类型
     */
    public TokenAuthentication(final Object principal, final Object credentials, final Integer type) {
        super(principal, credentials);
        this.type = type;
    }

    /**
     * 构造函数
     *
     * @param principal   认证账号
     * @param credentials 认证密码
     * @param authorities 用户角色集合
     * @param type        用户类型
     */
    public TokenAuthentication(final Object principal, final Object credentials, final Collection<? extends GrantedAuthority> authorities, final Integer type) {
        super(principal, credentials, authorities);
        this.type = type;
    }
}
