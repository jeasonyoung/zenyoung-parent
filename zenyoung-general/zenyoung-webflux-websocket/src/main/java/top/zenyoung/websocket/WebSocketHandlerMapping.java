package top.zenyoung.websocket;

import com.google.common.collect.Maps;
import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.Map;
import java.util.Objects;

/**
 * Websocket路径处理器
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/29 2:50 下午
 **/
public class WebSocketHandlerMapping extends SimpleUrlHandlerMapping {
    /**
     * 路径处理器集合
     */
    private final Map<String, WebSocketHandler> handlerMap = Maps.newLinkedHashMap();

    @Override
    public void initApplicationContext() throws BeansException {
        final Map<String, Object> beanMaps = obtainApplicationContext().getBeansWithAnnotation(WebSocketMapping.class);
        if (!CollectionUtils.isEmpty(beanMaps)) {
            beanMaps.values().forEach(bean -> {
                if (!(bean instanceof WebSocketHandler)) {
                    throw new RuntimeException(String.format(
                            "Controller [%s] doesn't implement WebSocketHandler interface.",
                            bean.getClass().getName()));
                }
                final WebSocketMapping annotation = AnnotationUtils.getAnnotation(bean.getClass(), WebSocketMapping.class);
                //WebSocketMapping映射
                handlerMap.put(Objects.requireNonNull(annotation).value(), (WebSocketHandler) bean);
            });
        }
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setUrlMap(handlerMap);
        super.initApplicationContext();
    }
}
