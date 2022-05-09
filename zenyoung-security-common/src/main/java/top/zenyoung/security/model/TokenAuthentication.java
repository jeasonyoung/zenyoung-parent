package top.zenyoung.security.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.annotation.Nonnull;

/**
 * 令牌认证
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 4:23 下午
 **/
public class TokenAuthentication<ReqBody extends LoginReqBody> extends UsernamePasswordAuthenticationToken {

    /**
     * 获取请求报文体
     */
    @Getter
    private final ReqBody reqBody;

    /**
     * 是否免密认证
     */
    @Getter
    @Setter
    private Boolean freePwdAuthen;

    /**
     * 构造函数
     *
     * @param reqBody 登录数据
     */
    public TokenAuthentication(@Nonnull final ReqBody reqBody, @Nonnull final Boolean freePwd) {
        super(reqBody.getAccount(), reqBody.getPasswd());
        this.reqBody = reqBody;
        this.freePwdAuthen = freePwd;
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
}
