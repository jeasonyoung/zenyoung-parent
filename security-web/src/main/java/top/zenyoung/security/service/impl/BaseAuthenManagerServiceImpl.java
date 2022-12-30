package top.zenyoung.security.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.security.service.AuthenManagerService;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * 安全认证管理-服务接口实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseAuthenManagerServiceImpl implements AuthenManagerService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();

    @Override
    public Authentication parseAuthenToken(@Nonnull final HttpServletRequest request) {
        final String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!Strings.isNullOrEmpty(token)) {
            synchronized (LOCKS.computeIfAbsent(token, k -> new Object())) {
                try {
                    String tokenVal = token.trim();
                    //检查是否有Bearer
                    final String bearer = "Bearer ";
                    if (token.startsWith(bearer)) {
                        tokenVal = token.replaceFirst(bearer, "").trim();
                    }
                    final UserPrincipal principal = buildPrincipal(tokenVal);
                    if (Objects.nonNull(principal)) {
                        return SecurityUtils.create(principal);
                    }
                } finally {
                    LOCKS.remove(token);
                }
            }
        }
        return null;
    }

    /**
     * 根据令牌构建用户信息
     *
     * @param token 令牌串
     * @return 用户信息
     */
    protected abstract UserPrincipal buildPrincipal(@Nonnull final String token);
}
