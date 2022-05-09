package top.zenyoung.security.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.common.util.ClassUtils;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.security.BaseRestfulAuthenticationManager;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.LoginRespBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.web.controller.util.RespJsonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * 令牌认证管理器
 *
 * @author young
 */
@Slf4j
public abstract class BaseMvcAuthenticationManager<ReqBody extends LoginReqBody> extends BaseRestfulAuthenticationManager<ReqBody> {
    /**
     * 获取ObjectMapper对象
     *
     * @return ObjectMapper对象
     */
    @Nonnull
    protected abstract ObjectMapper getObjMapper();

    /**
     * 解析请求报文
     *
     * @param inputStream  请求报文数据流
     * @param reqBodyClass 请求报文类型
     * @return 请求报文
     */
    public ReqBody parseReqBody(@Nonnull final InputStream inputStream, @Nonnull final Class<ReqBody> reqBodyClass) {
        return JsonUtils.fromStream(getObjMapper(), inputStream, reqBodyClass);
    }

    /**
     * 解析表单请求数据
     *
     * @param reqParams    表单数据
     * @param reqBodyClass 请求报文类型
     * @return 请求报文
     */
    @SneakyThrows({})
    public ReqBody parseFromData(@Nullable final Map<String, String[]> reqParams, @Nonnull final Class<ReqBody> reqBodyClass) {
        if (!CollectionUtils.isEmpty(reqParams)) {
            final ReqBody reqBody = reqBodyClass.newInstance();
            ClassUtils.getFieldHandlers(reqBodyClass, field -> {
                final String name = field.getName();
                if (!Strings.isNullOrEmpty(name)) {
                    final String[] vals = reqParams.get(name);
                    if (Objects.nonNull(vals) && vals.length > 0) {
                        try {
                            field.setAccessible(true);
                            field.set(reqBody, vals[0]);
                        } catch (Throwable ex) {
                            log.warn("parseFromData[name: {},val: {}]-exp: {}", name, vals[0], ex.getMessage());
                        }
                    }
                }
            });
            return reqBody;
        }
        return null;
    }

    /**
     * 构建认证前对象
     *
     * @param request 请求对象
     * @param reqBody 用户登录数据
     * @return 认证对象
     */
    public TokenAuthentication<ReqBody> buildBeforeAuthenticate(@Nonnull final ServletServerHttpRequest request, @Nonnull final ReqBody reqBody) {
        return new TokenAuthentication<>(reqBody, false);
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
     * 认证成功处理
     *
     * @param response  响应数据流
     * @param principal 登录成功用户信息
     */
    public void successfulAuthenticationHandler(@Nonnull final HttpServletResponse response, @Nonnull final UserPrincipal principal) {
        final LoginRespBody respBody = buildSuccessfulLoginBody(principal);
        RespJsonUtils.buildSuccessResp(getObjMapper(), response, respBody);
    }

    /**
     * 构建登录响应报文体
     *
     * @param principal 用户信息
     * @return 响应报文体
     */
    @Nonnull
    protected abstract LoginRespBody buildSuccessfulLoginBody(@Nonnull final UserPrincipal principal);

    /**
     * 认证失败处理
     *
     * @param response 响应数据流
     * @param failed   认证异常
     */
    public void unsuccessfulAuthentication(@Nonnull final HttpServletResponse response, @Nonnull final HttpStatus httpStatus, @Nullable final RuntimeException failed) {
        RespJsonUtils.buildFailResp(getObjMapper(), response, httpStatus, failed);
    }
}
