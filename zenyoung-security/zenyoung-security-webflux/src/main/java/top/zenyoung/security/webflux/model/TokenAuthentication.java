package top.zenyoung.security.webflux.model;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 令牌认证
 *
 * @author yangyong
 * @version 1.0
 *  2020/3/19 4:23 下午
 **/
public class TokenAuthentication extends UsernamePasswordAuthenticationToken {

    public TokenAuthentication(final Object principal, final Object credentials) {
        super(principal, credentials);
    }

    public TokenAuthentication(final Object principal, final Object credentials, final Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
