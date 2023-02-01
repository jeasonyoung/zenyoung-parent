package top.zenyoung.boot.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 安全服务工具类
 *
 * @author young
 */
public class SecurityUtils {
    /**
     * 创建认证存储对象
     *
     * @param userPrincipal 用户认证信息
     * @return 认证存储对象
     */
    public static Authentication create(@Nonnull final UserPrincipal userPrincipal) {
        final List<String> roles = userPrincipal.getRoles();
        final List<? extends GrantedAuthority> authorities = CollectionUtils.isEmpty(roles) ? Lists.newArrayList() :
                roles.stream()
                        .filter(role -> !Strings.isNullOrEmpty(role))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    }

    /**
     * 设置当前用户信息
     *
     * @param userPrincipal 当前用户信息
     */
    public static void setAuthentication(@Nonnull final UserPrincipal userPrincipal) {
        final Authentication authentication = create(userPrincipal);
        final SecurityContext ctx;
        if (Objects.nonNull(ctx = SecurityContextHolder.getContext())) {
            ctx.setAuthentication(authentication);
        }
    }

    /**
     * 获取当前用户认证信息
     *
     * @return 当前用户认证信息
     */
    public static Authentication getAuthentication() {
        final SecurityContext ctx;
        if (Objects.nonNull(ctx = SecurityContextHolder.getContext())) {
            return ctx.getAuthentication();
        }
        return null;
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    public static UserPrincipal getUser() {
        final Authentication auth = getAuthentication();
        if (Objects.nonNull(auth)) {
            return (UserPrincipal) auth.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    public static Optional<UserPrincipal> getUserOpt() {
        return Optional.ofNullable(getUser());
    }
}
