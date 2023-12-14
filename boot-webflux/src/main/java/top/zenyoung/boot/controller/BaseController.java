package top.zenyoung.boot.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * 控制器-基类
 *
 * @author yangyong
 */
public class BaseController {
    /**
     * 成功响应
     *
     * @param mono 业务数据
     * @param <T>  业务数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> success(@Nonnull final Mono<T> mono) {
        return mono.map(val -> {
            if (Objects.isNull(val)) {
                return ResultVO.ofSuccess();
            }
            return ResultVO.ofSuccess(val);
        });
    }

    /**
     * 成功响应
     *
     * @param flux 业务数据
     * @param <T>  业务数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<List<T>>> success(@Nonnull final Flux<T> flux) {
        return flux.collectList()
                .map(items -> {
                    if (CollectionUtils.isEmpty(items)) {
                        return ResultVO.ofSuccess(Lists.newArrayList());
                    }
                    return ResultVO.ofSuccess(items);
                });
    }

    /**
     * 成功响应
     *
     * @param <T> 响应结果类型
     * @return 响应结果
     */
    protected <T> Mono<ResultVO<T>> success() {
        return Mono.just(ResultVO.ofSuccess());
    }


    /**
     * 失败响应
     *
     * @param code    失败代码
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> failed(@Nullable final Integer code, @Nullable final String message) {
        return Mono.fromSupplier(() -> {
            final ResultVO<T> vo = ResultVO.ofFail();
            //失败代码
            if (Objects.nonNull(code)) {
                vo.setCode(code);
            }
            //失败消息
            if (!Strings.isNullOrEmpty(message)) {
                vo.setMessage(message);
            }
            return vo;
        });
    }


    /**
     * 失败响应
     *
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> failed() {
        return Mono.fromSupplier(ResultVO::ofFail);
    }
}
