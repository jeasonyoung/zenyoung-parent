package top.zenyoung.boot.util;

import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.zenyoung.boot.util.matcher.AntPathRequestMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetSocketAddress;
import java.util.LinkedList;
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
public class HttpUtils {
    private static final List<String> HTTP_CLIENT_IP_HEAD = new LinkedList<String>() {
        {
            add("x-forwarded-for");
            add("Proxy-Client-IP");
            add("WL-Proxy-Client-IP");
            add("X-Real-IP");
        }
    };

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
    public static String getClientIpAddr() {
        final HttpServletRequest request = getWebRequest();
        return request == null ? null : getClientIpAddr(request);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request request
     * @return 客户端IP地址
     */
    public static String getClientIpAddr(@Nonnull final HttpServletRequest request) {
        return getClientIpAddr(new ServletServerHttpRequest(request));
    }

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
    public static String getClientIpAddr(@Nullable final InetSocketAddress address) {
        if (address != null) {
            return Objects.requireNonNull(address).getAddress().getHostAddress();
        }
        return null;
    }

    /**
     * 获取请求内容类型
     *
     * @param request 请求对象
     * @return 请求内容类型
     */
    public static MediaType getContentType(@Nonnull final HttpServletRequest request) {
        final String requestContentType = request.getContentType();
        if (!Strings.isNullOrEmpty(requestContentType)) {
            return MediaType.parseMediaType(requestContentType);
        }
        return null;
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
    public static boolean isIE(@Nonnull final HttpServletRequest request) {
        return (request.getHeader("USER-AGENT").toLowerCase().indexOf("msie") > 0
                || request.getHeader("USER-AGENT").toLowerCase().indexOf("rv:11.0") > 0
                || request.getHeader("USER-AGENT").toLowerCase().indexOf("edge") > 0) ? true
                : false;
    }
}
