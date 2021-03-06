package top.zenyoung.web.controller.global;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;
import top.zenyoung.web.vo.RespResult;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 全局错误处理
 *
 * @author yangyong
 * @version 1.0
 * 2020/4/1 2:29 下午
 **/
@Slf4j
public abstract class BaseGlobalErrorExceptionHandler extends AbstractErrorWebExceptionHandler {
    
    public BaseGlobalErrorExceptionHandler(final ErrorAttributes errorAttributes, final WebProperties.Resources resources, final ApplicationContext applicationContext) {
        super(errorAttributes, resources, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Nonnull
    protected Mono<ServerResponse> renderErrorResponse(final ServerRequest request) {
        final Map<String, Object> errors = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        log.error("renderErrorResponse=>\n {}", errors);
        //返回数据处理
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(RespResult.of(getHttpStatus(errors), getMessage(errors), Maps.newLinkedHashMap(errors))));
    }

    protected int getHttpStatus(final Map<String, Object> errorAttributes) {
        return (int) errorAttributes.get("status");
    }

    protected String getMessage(final Map<String, Object> errorAttributes) {
        return (String) errorAttributes.get("message");
    }
}
