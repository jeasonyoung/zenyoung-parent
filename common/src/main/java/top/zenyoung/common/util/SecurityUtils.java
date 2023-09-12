package top.zenyoung.common.util;

import lombok.experimental.UtilityClass;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 安全服务工具类
 *
 * @author young
 */
@UtilityClass
public class SecurityUtils {
    private static final ThreadLocal<AtomicReference<UserPrincipal>> LOCAL_REF = ThreadLocal.withInitial(() -> new AtomicReference<>(null));

    /**
     * 设置当前用户信息
     *
     * @param principal 当前用户信息
     */
    public static void setPrincipal(@Nonnull final UserPrincipal principal) {
        Optional.ofNullable(LOCAL_REF.get())
                .ifPresent(ref -> ref.set(principal));
    }

    /**
     * 获取当前用户认证信息
     *
     * @return 当前用户认证信息
     */
    public static UserPrincipal getPrincipal() {
        return Optional.ofNullable(LOCAL_REF.get())
                .map(AtomicReference::get)
                .orElse(null);
    }
}
