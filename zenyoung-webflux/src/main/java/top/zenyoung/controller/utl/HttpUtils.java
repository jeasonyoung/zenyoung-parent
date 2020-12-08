package top.zenyoung.controller.utl;

import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Http 工具类
 *
 * @author yangyong
 * @version 1.0
 **/
public class HttpUtils {
    private static final String UNKNOWN = "unknown";

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
            String ipAddr = headers.getFirst("x-forwarded-for");
            if (Strings.isNullOrEmpty(ipAddr) || UNKNOWN.equalsIgnoreCase(ipAddr)) {
                ipAddr = headers.getFirst("Proxy-Client-IP");
            }
            if (Strings.isNullOrEmpty(ipAddr) || UNKNOWN.equalsIgnoreCase(ipAddr)) {
                ipAddr = headers.getFirst("WL-Proxy-Client-IP");
            }
            if (Strings.isNullOrEmpty(ipAddr) || UNKNOWN.equalsIgnoreCase(ipAddr)) {
                ipAddr = headers.getFirst("X-Real-IP");
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
