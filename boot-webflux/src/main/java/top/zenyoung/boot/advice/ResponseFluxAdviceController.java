package top.zenyoung.boot.advice;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;

/**
 * 统一异常处理控制器
 *
 * @author young
 */
@RestControllerAdvice
public class ResponseFluxAdviceController extends BaseResponseAdviceController<Mono<ResultVO<?>>> {

    @Override
    protected <T> Mono<ResultVO<?>> resultHandler(@Nonnull final ResultVO<T> vo) {
        return Mono.just(vo);
    }
}
