package top.zenyoung.security.util;

import com.google.common.base.Joiner;
import top.zenyoung.security.auth.Ticket;

import javax.annotation.Nonnull;

/**
 * 常量
 *
 * @author young
 */
public class Constants {
    /**
     * redis分隔符
     */
    public static final String SEP_REDIS = ":";
    /**
     * 常量前缀
     */
    public static final String PREFIX = "zy-framework" + SEP_REDIS;

    /**
     * 认证-刷新令牌前缀
     */
    public static final String AUTH_REFRESH_TOKEN_PREFIX = PREFIX + "refresh-token" + SEP_REDIS;
    /**
     * 认证-令牌关联刷新令牌前缀
     */
    public static final String AUTH_TOKEN_WITH_REFRESH_PREFIX = PREFIX + "token-with-refresh" + SEP_REDIS;
    /**
     * 认证-刷新令牌关联令牌前缀
     */
    public static final String AUTH_REFRESH_WITH_TOKEN_PREFIX = PREFIX + "refresh-with-token" + SEP_REDIS;

    /**
     * 拼接串
     *
     * @param joins 拼接集合
     * @return 拼接后字符串
     */
    public static String join(@Nonnull final String... joins) {
        return Joiner.on(SEP_REDIS).skipNulls().join(joins);
    }

    /**
     * 根据用户票据获取认证刷新令牌缓存键
     *
     * @param ticket 用户票据
     * @return 刷新令牌缓存键
     */
    public static String getAuthRefreshWithTicketKey(@Nonnull final Ticket ticket) {
        return Constants.AUTH_REFRESH_TOKEN_PREFIX + Constants.join(ticket.getDevice(), ticket.getDevice(), ticket.getId());
    }

    /**
     * 根据刷新令牌获取访问令牌缓存键
     *
     * @param refreshToken 刷新令牌
     * @return 访问令牌缓存键
     */
    public static String getAuthAccessWithRefreshKey(@Nonnull final String refreshToken) {
        return AUTH_TOKEN_WITH_REFRESH_PREFIX + refreshToken;
    }

    /**
     * 根据刷新令牌获取票据缓存键
     *
     * @param refreshToken 刷新令牌
     * @return 票据缓存键
     */
    public static String getAuthTicketWithRefreshKey(@Nonnull final String refreshToken) {
        return AUTH_REFRESH_TOKEN_PREFIX + join("ticket", refreshToken);
    }

    /**
     * 根据访问令牌获取刷新令牌缓存键
     *
     * @param accessToken 访问令牌
     * @return 刷新令牌缓存键
     */
    public static String getAuthRefreshWithAccessKey(@Nonnull final String accessToken) {
        return AUTH_REFRESH_WITH_TOKEN_PREFIX + accessToken;
    }
}
