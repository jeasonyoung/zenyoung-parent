package top.zenyoung.websocket;

import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import top.zenyoung.websocket.common.WebSocketMapping;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Websocket路径处理器
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/29 2:50 下午
 **/
public class WebSocketHandlerMapping extends SimpleUrlHandlerMapping {

    @Override
    public void initApplicationContext() throws BeansException {
        final Map<String, Object> beanMaps = obtainApplicationContext().getBeansWithAnnotation(WebSocketMapping.class);
        if (!CollectionUtils.isEmpty(beanMaps)) {
            final Map<String, WebSocketHandler> handlers = beanMaps.values().stream().map(bean -> {
                if (!(bean instanceof WebSocketHandler)) {
                    throw new RuntimeException(String.format(
                            "Controller [%s] doesn't implement WebSocketHandler interface.",
                            bean.getClass().getName()));
                }
                final WebSocketMapping annotation = AnnotationUtils.getAnnotation(bean.getClass(), WebSocketMapping.class);
                if (annotation != null) {
                    return new Map.Entry<String, WebSocketHandler>() {

                        @Override
                        public String getKey() {
                            return annotation.value();
                        }

                        @Override
                        public WebSocketHandler getValue() {
                            return (WebSocketHandler) bean;
                        }

                        @Override
                        public WebSocketHandler setValue(WebSocketHandler value) {
                            return null;
                        }
                    };
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> n));
            if (!CollectionUtils.isEmpty(handlers)) {
                setOrder(Ordered.HIGHEST_PRECEDENCE);
                setUrlMap(handlers);
            }
        }
        super.initApplicationContext();
    }
}
