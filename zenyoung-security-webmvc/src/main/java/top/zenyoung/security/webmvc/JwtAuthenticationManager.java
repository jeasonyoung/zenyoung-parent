package top.zenyoung.security.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.security.BaseJwtAuthenticationManager;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.model.TokenUserDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Map;

/**
 * 令牌认证管理器
 *
 * @author young
 */
@Slf4j
public abstract class JwtAuthenticationManager<ReqBody extends LoginReqBody> extends BaseJwtAuthenticationManager<ReqBody> implements AuthenticationManager {
    private final static Map<Class<?>, DaoAuthenticationProvider> DAO_PROVIDERS = Maps.newConcurrentMap();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析请求报文
     *
     * @param inputStream  请求报文数据流
     * @param reqBodyClass 请求报文类型
     * @return 请求报文
     */
    public ReqBody parseReqBody(@Nonnull final InputStream inputStream, @Nonnull final Class<ReqBody> reqBodyClass) {
        log.debug("parseReqBody(reqBodyClass: {})...", reqBodyClass);
        try {
            return objectMapper.readValue(inputStream, reqBodyClass);
        } catch (Throwable ex) {
            log.error("parseReqBody(reqBodyClass: {})-exp: {}", reqBodyClass, ex.getMessage());
        }
        return null;
    }

    /**
     * 解析表单请求数据
     *
     * @param reqBody   请求对象
     * @param reqParams 请求参数集合
     */
    public void parseFromData(@Nonnull final ReqBody reqBody, @Nullable final Map<String, String[]> reqParams) {

    }

    /**
     * 解析用户认证令牌
     *
     * @param request 请求对象
     * @return 用户认证令牌
     */
    public TokenAuthentication<ReqBody> parseAuthenticationToken(@Nonnull final HttpServletRequest request) throws TokenException {
        return parseAuthenticationToken(request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /**
     * 构建用户认证服务实现
     *
     * @param reqBody 请求数据
     * @return 认证服务实现
     */
    protected UserDetailsService buildUserDetailsService(@Nonnull final ReqBody reqBody) {
        log.debug("buildUserDetailsService(reqBody: {})...", reqBody);
        return username -> {
            try {
                //认证前校验
                preAuthenticationChecked(reqBody);
                //调用用户认证
                final TokenUserDetails userDetails = userAuthenHandler(reqBody);
                if (userDetails == null) {
                    throw new UsernameNotFoundException("用户名不存在!");
                }
                return userDetails;
            } catch (AuthenticationException ex) {
                log.warn("buildUserDetailsService(reqBody: {})-exp: {}", reqBody, ex.getMessage());
                throw ex;
            }
        };
    }

    /**
     * 认证处理
     *
     * @param authentication 认证数据
     * @return 认证结果
     * @throws AuthenticationException 认证异常
     */
    @SuppressWarnings("unchecked")
    @Override
    public final Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        log.debug("authenticate(authentication: {})...", authentication);
        //初始化认证处理器
        final DaoAuthenticationProvider provider = DAO_PROVIDERS.computeIfAbsent(DaoAuthenticationProvider.class, k -> getAuthenticationProvider());
        //设置密码编码器
        provider.setPasswordEncoder(getPasswordEncoder());
        //认证数据
        TokenAuthentication<ReqBody> tokenAuthen = null;
        if (authentication instanceof TokenAuthentication) {
            tokenAuthen = (TokenAuthentication<ReqBody>) authentication;
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            tokenAuthen = new TokenAuthentication<>((UsernamePasswordAuthenticationToken) authentication, this);
        }
        //检查认证数据
        if (tokenAuthen == null) {
            throw new BadCredentialsException("获取认证数据失败!=>" + authentication);
        }
        //设置用户认证服务
        provider.setUserDetailsService(buildUserDetailsService(tokenAuthen.getReqBody()));
        //认证处理
        return provider.authenticate(tokenAuthen);
    }

    /**
     * 获取Dao认证提供者实现
     *
     * @return Dao认证提供者
     */
    protected DaoAuthenticationProvider getAuthenticationProvider() {
        return new DaoAuthenticationProvider();
    }

    /**
     * 认证成功处理
     *
     * @param response  响应数据流
     * @param principal 登录成功用户信息
     */
    public abstract void successfulAuthenticationHandler(@Nonnull final HttpServletResponse response, @Nonnull final UserPrincipal principal);

    /**
     * 认证失败处理
     *
     * @param response 响应数据流
     * @param failed   认证异常
     */
    public abstract void unsuccessfulAuthentication(@Nonnull final HttpServletResponse response, @Nullable final AuthenticationException failed);
}
