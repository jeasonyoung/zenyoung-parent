package top.zenyoung.security.webflux;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import top.zenyoung.security.spi.auth.RespLoginBody;
import top.zenyoung.security.spi.token.TokenDetail;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
     * 绑定用户处理
     *
     * @param tokenDetail 令牌用户数据
     * @param bindType    绑定用户类型
     * @param bindId      绑定用户ID
     */
    void bindUserHandler(@Nonnull final TokenDetail tokenDetail, @Nullable final Integer bindType, @Nullable final String bindId);
}
