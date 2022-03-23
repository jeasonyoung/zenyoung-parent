package top.zenyoung.web.controller.util;

import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Http 工具类
 *
 * @author young
 */
public class HttpUtils {
    private static final List<String> HTTP_CLIENT_IP_HEAD = new LinkedList<>() {
        {
            add("x-forwarded-for");
            add("Proxy-Client-IP");
            add("WL-Proxy-Client-IP");
            add("X-Real-IP");
        }
    };

    /**
     * 获取当前请求
     *
     * @return 当前请求
     */
    public static HttpServletRequest getWebRequest() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs.getRequest();
        }
        return null;
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
}
