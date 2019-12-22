package top.zenyoung.security.webflux;

/**
 * 认证过滤器接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:42 下午
 **/
public interface AuthFilter {

    /**
     * 获取认证登录URLs
     *
     * @return 认证登录URLs
     */
    default String[] getAuthLoginUrls() {
        return new String[]{"/login", "/auth/login", "/app/auth/login"};
    }
}
