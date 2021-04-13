package top.zenyoung.websocket;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * WebSocket-控制器接口
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/29 4:19 下午
 **/
public interface WebSocketController extends WebSocketHandler {

    /**
     * 获取URL参数
     *
     * @param handshakeInfo 握手请求数据
     * @return 参数数据
     */
    default Map<String, String> getUrlQueries(@Nonnull final HandshakeInfo handshakeInfo) {
        final String query = handshakeInfo.getUri().getQuery();
        if (Strings.isNullOrEmpty(query)) {
            return null;
        }
        return StreamSupport.stream(Splitter.on("&").omitEmptyStrings().trimResults().split(query).spliterator(), false)
                .map(s -> {
                    final String[] kv = s.split("=", 2);
                    return new Map.Entry<String, String>() {

                        @Override
                        public String getKey() {
                            return kv[0];
                        }

                        @Override
                        public String getValue() {
                            return kv.length == 2 ? kv[1] : "";
                        }

                        @Override
                        public String setValue(final String value) {
                            return null;
                        }
                    };
                })
                .collect(Collectors.toMap(o -> o.getKey(), o -> o.getValue(), (n, o) -> n));
    }
}
