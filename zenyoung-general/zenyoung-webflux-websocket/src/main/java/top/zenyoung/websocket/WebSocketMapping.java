package top.zenyoung.websocket;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.lang.annotation.*;

/**
 * Websocket路径注解
 *
 * @author yangyong
 * date 2020/6/29 2:50 下午
 */
@Indexed
@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketMapping {

    /**
     * WebSocket路径
     *
     * @return 路径
     */
    String value() default "";
}
