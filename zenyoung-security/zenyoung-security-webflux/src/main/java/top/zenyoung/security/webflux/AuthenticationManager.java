package top.zenyoung.security.webflux;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import top.zenyoung.security.spi.auth.RespLoginBody;
import top.zenyoung.security.spi.token.TokenAuthentication;
import top.zenyoung.security.spi.token.TokenDetail;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * 用户认证管理器
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:18 下午
 **/
public interface AuthenticationManager extends ReactiveAuthenticationManager {

    /**
     * 获取用户认证响应数据
     *
     * @param tokenDetail 令牌用户数据
     * @return 用户认证响应数据
     */
    RespLoginBody getUserResp(@Nonnull final TokenDetail tokenDetail);

    /**
     * 检查请求头消息处理
     *
     * @return 请求头消息处理
     */
    default RequestHeaderHandler checkRequestHeaders() {
        return null;
    }

    /**
     * 请求消息头处理
     */
    interface RequestHeaderHandler {

        /**
         * 获取请求头字段数组
         *
         * @return 请求头字段数组
         */
        String[] getHeaderNames();

        /**
         * 认证前请求头处理
         *
         * @param reqHeaderValueMaps 请求头字段值Map
         * @param token              登录认证数据
         */
        default void beforeAuthenHandler(@Nonnull final Map<String, List<String>> reqHeaderValueMaps, @Nonnull final TokenAuthentication token) {

        }

        /**
         * 认证后请求头处理
         *
         * @param reqHeaderValueMaps 请求头字段值Map
         * @param tokenDetail        登录令牌数据
         */
        default void afterAuthenHandler(@Nonnull final Map<String, List<String>> reqHeaderValueMaps, @Nonnull final TokenDetail tokenDetail) {

        }
    }
}
