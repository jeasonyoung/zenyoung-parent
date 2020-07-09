package top.zenyoung.controller.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.RespResult;

import java.io.Serializable;

/**
 * 全局-控制器
 *
 * @author yangyong
 * @version 1.0
 *  2020/4/1 11:56 上午
 **/
@Slf4j
public abstract class BaseGlobalController {

    /**
     * 全局异常捕获
     *
     * @param e 异常内容
     * @return 响应消息
     */
    @ExceptionHandler({Throwable.class})
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<RespResult<Serializable>> handlerException(final Throwable e) {
        log.debug("handlerException", e);
        return Mono.create(sink -> sink.success(RespResult.ofFail(e.getMessage())));
    }
}
