package top.zenyoung.web.controller.util;

import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Http 工具类
 *
 * @author yangyong
 * @version 1.0
 **/
public class HttpUtils {
    private static final String UNKNOWN = "unknown";

    private static final List<String> HTTP_CLIENT_IP_HEAD = new LinkedList<>() {
        {
            add("x-forwarded-for");
            add("Proxy-Client-IP");
            add("WL-Proxy-Client-IP");
            add("X-Real-IP");
        }
    };

    /**
     * 获取客户端IP地址
     *
     * @param request ServerHttpRequest
     * @return 客户端IP地址
     */
    public static String getClientIpAddr(@Nonnull final ServerHttpRequest request) {
        String ipAddr = getClientIpAddr(request.getHeaders());
        if (Strings.isNullOrEmpty(ipAddr) || UNKNOWN.equalsIgnoreCase(ipAddr)) {
            ipAddr = getClientIpAddr(request.getRemoteAddress());
        }
        return ipAddr;
    }

    /**
     * 获取客户端IP地址
     *
     * @param headers HttpHeaders
     * @return 客户端IP地址
     */
    public static String getClientIpAddr(@Nullable final HttpHeaders headers) {
        if (!CollectionUtils.isEmpty(headers)) {
            String ipAddr = null;
            for (String head : HTTP_CLIENT_IP_HEAD) {
                ipAddr = headers.getFirst(head);
                if (!Strings.isNullOrEmpty(ipAddr) && !UNKNOWN.equalsIgnoreCase(ipAddr)) {
                    return ipAddr;
                }
            }
            return ipAddr;
        }
        return null;
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
