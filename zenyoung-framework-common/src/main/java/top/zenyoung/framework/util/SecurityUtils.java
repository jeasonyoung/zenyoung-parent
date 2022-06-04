package top.zenyoung.framework.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import top.zenyoung.common.model.UserPrincipal;

import java.util.Optional;

/**
 * 安全服务工具类
 *
 * @author young
 */
public class SecurityUtils {

    /**
     * 获取当前用户认证信息
     *
     * @return 当前用户认证信息
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    public static UserPrincipal getUser() {
        return (UserPrincipal) getAuthentication().getPrincipal();
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
