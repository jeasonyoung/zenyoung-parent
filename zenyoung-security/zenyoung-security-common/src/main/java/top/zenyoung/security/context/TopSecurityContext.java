package top.zenyoung.security.context;

import lombok.Getter;
import org.springframework.http.HttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 安全上下文对象
 *
 * @author yangyong
 * @version 1.0
 **/
public class TopSecurityContext implements SecurityContext {
    private final SecurityContext context;

    @Getter
    private final HttpRequest request;

    public TopSecurityContext(@Nonnull final SecurityContext context, @Nullable final HttpRequest request) {
        this.context = context;
        this.request = request;
    }

    @Override
    public Authentication getAuthentication() {
        return context.getAuthentication();
    }

    @Override
    public void setAuthentication(final Authentication authentication) {
        context.setAuthentication(authentication);
    }
}
