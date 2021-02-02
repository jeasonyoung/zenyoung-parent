package top.zenyoung.controller.util;

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Http 工具类
 *
 * @author young
 */
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
     * @param request request
     * @return 客户端IP地址
     */
    public static String getClientIpAddr(@Nonnull final HttpServletRequest request) {
        String ipAddr = request.getRemoteAddr();
        if (Strings.isNullOrEmpty(ipAddr)) {
            for (String head : HTTP_CLIENT_IP_HEAD) {
                ipAddr = request.getHeader(head);
                if (!Strings.isNullOrEmpty(ipAddr) && !UNKNOWN.equalsIgnoreCase(ipAddr)) {
                    return ipAddr;
                }
            }
        }
        return ipAddr;
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
