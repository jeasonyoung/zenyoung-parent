package top.zenyoung.controller.utl;

import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
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

    public static String getClientIpAddr(@Nonnull final ServerHttpRequest request) {
        final HttpHeaders headers = request.getHeaders();
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
            if (Strings.isNullOrEmpty(ipAddr) || UNKNOWN.equalsIgnoreCase(ipAddr)) {
                final InetSocketAddress address = request.getRemoteAddress();
                ipAddr = Objects.requireNonNull(address).getAddress().getHostAddress();
            }
            return ipAddr;
        }
        return null;
    }
}
