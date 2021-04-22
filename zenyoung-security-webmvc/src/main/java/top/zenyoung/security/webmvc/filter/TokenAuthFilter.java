package top.zenyoung.security.webmvc.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.model.TokenUserDetails;
import top.zenyoung.security.token.TokenGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Jwt认证过滤器
 *
 * @author young
 */
@Slf4j
public class TokenAuthFilter extends UsernamePasswordAuthenticationFilter {
    private final TokenGenerator generator;
    private final Class<? extends LoginReqBody> reqBodyClass;
    private final ObjectMapper objMapper;

    public TokenAuthFilter(@Nonnull final TokenGenerator generator, @Nullable final Class<? extends LoginReqBody> reqBodyClass, @Nullable final ObjectMapper objMapper) {
        this.generator = generator;
        this.reqBodyClass = reqBodyClass == null ? LoginReqBody.class : reqBodyClass;
        this.objMapper = objMapper == null ? new ObjectMapper() : objMapper;
    }

    public TokenAuthFilter(@Nonnull final TokenGenerator generator) {
        this(generator, null, null);
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response) throws AuthenticationException {
        //构建请求Path
        final RequestPath requestPath = RequestPath.parse(request.getRequestURI(), request.getContextPath());
        //检查是否有令牌数据
        final String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!Strings.isNullOrEmpty(token)) {
            //解析令牌
            return parseToken(requestPath, token);
        }
        //检查是否为json
        if (MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(request.getContentType()))) {
            try {
                final ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);
                final LoginReqBody reqBody = objMapper.readValue(httpRequest.getBody(), reqBodyClass);
                final TokenAuthentication tokenAuthentication = new TokenAuthentication(requestPath, reqBody.getAccount(), reqBody.getPasswd(), reqBody);
                setDetails(request, tokenAuthentication);
                //认证处理
                return getAuthenticationManager().authenticate(tokenAuthentication);
            } catch (Throwable ex) {
                log.error("attemptAuthentication-exp: {}", ex.getMessage());
                throw new AuthenticationException(ex.getMessage(), ex) {

                };
            }
        }
        //表单验证
        return super.attemptAuthentication(request, response);
    }

    @Nonnull
    protected Authentication parseToken(@Nonnull final RequestPath path, @Nullable final String authorization) throws AuthenticationException {
        log.debug("parseToken(authorization: {})...", authorization);
        if (Strings.isNullOrEmpty(authorization)) {
            throw new TokenException("令牌为空");
        }
        //解析令牌
        final TokenUserDetails userDetails = new TokenUserDetails(generator.parseToken(authorization));
        log.info("parseToken(authorization: {})=> {}", authorization, userDetails);
        //转换用户数据
        return new TokenAuthentication(path, userDetails, null, userDetails.getAuthorities());
    }
}
