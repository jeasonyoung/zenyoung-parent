package top.zenyoung.security.webflux;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.LoginRespBody;
import top.zenyoung.security.model.UserPrincipal;
import top.zenyoung.security.token.JwtToken;
import top.zenyoung.security.token.Token;

import javax.annotation.Nonnull;

/**
 * 认证管理器接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/20 5:59 下午
 **/
public interface AuthenticationManager extends ReactiveAuthenticationManager {

    /**
     * 获取令牌
     *
     * @return 令牌
     */
    default Token getToken() {
        return new JwtToken();
    }

    /**
     * 获取登录请求方法
     *
     * @return 登录请求方法
     */
    default HttpMethod getLoginMethod() {
        return HttpMethod.POST;
    }

    /**
     * 获取登录请求地址集合
     *
     * @return 登录请求地址集合
     */
    default String[] getLoginUrls() {
        return new String[0];
    }

    /**
     * 获取用户登录请求报文类型
     *
     * @return 用户登录请求报文类型
     */
    default Class<? extends LoginReqBody> getLoginReqBodyClass() {
        return LoginReqBody.class;
    }

    /**
     * 获取返回登录用户数据
     *
     * @param userPrincipal 用户数据
     * @return 返回登录用户数据
     */
    LoginRespBody getUserResp(@Nonnull final UserPrincipal userPrincipal);
}