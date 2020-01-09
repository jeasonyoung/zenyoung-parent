package top.zenyoung.security.spi;

/**
 * 认证-常量
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/9 9:34 下午
 **/
public interface Constants {
    /**
     * 未登录
     */
    int UNAUTHENTION = 401;
    /**
     * 访问拒绝
     */
    int ACCESS_DENIED = 403;
    /**
     * 登录令牌无效
     */
    int LOGIN_TOKEN_INVALID = 412;
    /**
     * 登录令牌过期
     */
    int LOGIN_TOKEN_EXPIRE = 413;
    /**
     * 刷新令牌无效
     */
    int LOGIN_REFRESH_TOKEN_INVALID = 414;
}
