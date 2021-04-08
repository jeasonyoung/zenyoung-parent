package top.zenyoung.security.model;

import lombok.Getter;
import org.springframework.http.server.RequestPath;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * 令牌认证
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 4:23 下午
 **/
public class TokenAuthentication extends UsernamePasswordAuthenticationToken {

    @Getter
    private final RequestPath path;

    @Getter
    private LoginReqBody reqBody;

    public TokenAuthentication(@Nonnull final RequestPath path, @Nonnull final Object principal, @Nonnull final Object credentials, @Nonnull final LoginReqBody reqBody) {
        super(principal, credentials);
        this.path = path;
        this.reqBody = reqBody;
    }

    public TokenAuthentication(@Nonnull final RequestPath path, @Nonnull final Object principal, @Nullable final Object credentials, @Nonnull final Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.path = path;
    }
}
