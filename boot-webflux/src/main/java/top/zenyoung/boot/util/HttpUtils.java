package top.zenyoung.boot.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Http 工具类
 *
 * @author young
 */
@UtilityClass
public class HttpUtils {
    private static final List<String> HTTP_CLIENT_IP_HEAD = Lists.newArrayList("x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP", "X-Real-IP");

    /**
     * 获取客户端IP地址
     *
     * @param swe ServerWebExchange
     * @return 客户端IP地址
     */
    public static String getClientIpAddr(@Nonnull final ServerWebExchange swe) {
        return Optional.of(swe.getRequest())
                .map(HttpUtils::getClientIpAddr)
                .orElse(null);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request request
     * @return 客户端IP地址
     */
    public static String getClientIpAddr(@Nonnull final ServerHttpRequest request) {
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
