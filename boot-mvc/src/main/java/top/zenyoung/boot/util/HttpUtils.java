package top.zenyoung.boot.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.zenyoung.boot.matcher.AntPathRequestMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Http 工具类
 *
 * @author young
 */
@UtilityClass
public class HttpUtils {
    private static final List<String> HTTP_CLIENT_IP_HEAD = Lists.newArrayList(
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "X-Real-IP"
    );

    public static void servlet(@Nonnull final BiConsumer<HttpServletRequest, HttpServletResponse> handler) {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (Objects.nonNull(attrs)) {
                handler.accept(attrs.getRequest(), attrs.getResponse());
            }
        }
    }

    /**
     * 获取当前请求
     *
     * @return 当前请求
     */
    @Nullable
    public static HttpServletRequest getWebRequest() {
        final AtomicReference<HttpServletRequest> refReq = new AtomicReference<>(null);
        servlet((req, res) -> refReq.set(req));
        return refReq.get();
    }

    public static Optional<HttpServletRequest> getWebRequestOpt() {
        return Optional.ofNullable(getWebRequest());
    }

    /**
     * 获取当前请求客户端IP地址
     *
     * @return 客户端IP地址
     */
    @Nullable
    public static String getClientIpAddr() {
        return getWebRequestOpt().map(HttpUtils::getClientIpAddr).orElse(null);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request request
     * @return 客户端IP地址
     */
    @Nullable
    public static String getClientIpAddr(@Nonnull final HttpServletRequest request) {
        return getClientIpAddr(new ServletServerHttpRequest(request));
    }

    @Nullable
    public static String getClientIpAddr(@Nonnull final ServletServerHttpRequest request) {
        final HttpHeaders headers = request.getHeaders();
        for (String head : HTTP_CLIENT_IP_HEAD) {
            if (!Strings.isNullOrEmpty(head)) {
                final List<String> headVals = headers.getOrEmpty(head);
                if (!CollectionUtils.isEmpty(headers)) {
                    for (String val : headVals) {
                        if (!Strings.isNullOrEmpty(val)) {
                            return val;
                        }
                    }
                }
            }
        }
        return getClientIpAddr(request.getRemoteAddress());
    }

    /**
     * 获取客户端IP地址
     *
     * @param address InetSocketAddress
     * @return 客户端IP地址
     */
    @Nullable
    public static String getClientIpAddr(@Nullable final InetSocketAddress address) {
        return Optional.ofNullable(address)
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress)
                .orElse(null);
    }

    /**
     * 获取请求内容类型
     *
     * @param request 请求对象
     * @return 请求内容类型
     */
    @Nullable
    public static MediaType getContentType(@Nonnull final HttpServletRequest request) {
        return Optional.ofNullable(request.getContentType())
                .filter(requestContentType -> !Strings.isNullOrEmpty(requestContentType))
                .map(MediaType::parseMediaType)
                .orElse(null);
    }

    /**
     * 判断请求是否匹配
     *
     * @param request  请求对象
     * @param patterns 匹配集合
     * @return 匹配结果
     */
    public static boolean matches(@Nonnull final HttpServletRequest request, @Nonnull final String... patterns) {
        return Stream.of(patterns)
                .filter(pattern -> !Strings.isNullOrEmpty(pattern))
                .map(AntPathRequestMatcher::new)
                .anyMatch(p -> p.matches(request));
    }

    /**
     * 判断是否为IE浏览器
     *
     * @param request 请求对象
     * @return 是否为IE浏览器
     */
    public static boolean isIe(@Nonnull final HttpServletRequest request) {
        final String agent = request.getHeader("USER-AGENT");
        if (!Strings.isNullOrEmpty(agent)) {
            final String lowerCaseAgent = agent.toLowerCase();
            final List<String> targets = Lists.newArrayList("msie", "rv:11.0", "edge");
            for (final String target : targets) {
                if (lowerCaseAgent.contains(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取令牌数据
     *
     * @param request Http请求
     * @return 令牌数据
     */
    @Nullable
    public static String getToken(@Nonnull final HttpServletRequest request) {
        final String name = HttpHeaders.AUTHORIZATION, prefix = "Bearer ";
        //获取令牌
        final String token = Optional.ofNullable(request.getHeader(name))
                .filter(val -> !Strings.isNullOrEmpty(val))
                .orElseGet(() -> request.getParameter(name));
        if (!Strings.isNullOrEmpty(token)) {
            return token.startsWith(prefix) ? StringUtils.replace(token, prefix, "") : token;
        }
        return null;
    }

    /**
     * 获取令牌数据
     *
     * @return 令牌数据
     */
    @Nullable
    public static String getToken() {
        return getWebRequestOpt().map(HttpUtils::getToken).orElse(null);
    }
}
