package top.zenyoung.boot.advice;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;

/**
 * 统一异常处理控制器
 *
 * @author young
 */
@RestControllerAdvice
public class ResponseMvcAdviceController extends BaseResponseAdviceController<ResultVO<?>> {
    @Override
    protected <T> ResultVO<?> resultHandler(@Nonnull final ResultVO<T> vo) {
        return vo;
    }
}
