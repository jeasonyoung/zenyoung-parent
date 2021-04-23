package top.zenyoung.security.model;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.annotation.Nonnull;

/**
 * 令牌认证
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 4:23 下午
 **/
public class TokenAuthentication extends UsernamePasswordAuthenticationToken {

    /**
     * 获取请求报文体
     */
    @Getter
    private final LoginReqBody reqBody;

    /**
     * 构造函数
     *
     * @param reqBody 登录数据
     */
    public TokenAuthentication(@Nonnull final LoginReqBody reqBody) {
        super(reqBody.getAccount(), reqBody.getPasswd());
        this.reqBody = reqBody;
    }

    /**
     * 构造函数
     *
     * @param userDetails 登录用户怇
     */
    public TokenAuthentication(@Nonnull final TokenUserDetails userDetails) {
        super(userDetails, null, userDetails.getAuthorities());
        this.reqBody = null;
    }

    /**
     * 构造函数
     *
     * @param token 用户密码认证令牌
     */
    public TokenAuthentication(@Nonnull final UsernamePasswordAuthenticationToken token) {
        super(token.getPrincipal(), token.getCredentials());
        reqBody = new LoginReqBody();
        reqBody.setAccount((String) token.getPrincipal());
        reqBody.setPasswd((String) token.getCredentials());
    }
}
