package top.zenyoung.security.webflux;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import top.zenyoung.security.spi.auth.RespLoginBody;
import top.zenyoung.security.spi.token.TokenDetail;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
     * 绑定用户处理
     *
     * @param tokenDetail 令牌用户数据
     * @param bindType    绑定用户类型
     * @param bindId      绑定用户ID
     */
    default void bindUserHandler(@Nonnull final TokenDetail tokenDetail, @Nullable final Integer bindType, @Nullable final String bindId) {

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
         * 请求头字段值检查处理
         *
         * @param requestHeaderValueMaps 请求头字段值Map
         * @throws Exception 异常
         */
        void headerValuesHandler(@Nonnull final Map<String, List<String>> requestHeaderValueMaps) throws Exception;
    }
}
