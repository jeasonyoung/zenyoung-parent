package top.zenyoung.boot.controller;

import com.google.common.base.Strings;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nullable;
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
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> successByData(@Nullable final Mono<T> data) {
        if (Objects.isNull(data)) {
            return Mono.just(ResultVO.ofSuccess());
        }
        return data.map(ResultVO::ofSuccess)
                .onErrorResume(e -> Mono.just(ResultVO.ofFail(e)));
    }

    /**
     * 成功响应
     *
     * @param dataResult 业务数据
     * @param <T>        数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<PageList<T>>> successByPage(@Nullable final Mono<PageList<T>> dataResult) {
        if (Objects.isNull(dataResult)) {
            return Mono.just(ResultVO.ofSuccess());
        }
        return dataResult.map(ResultVO::ofSuccess)
                .onErrorResume(e -> Mono.just(ResultVO.ofFail(e)));
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
     * @param data    失败数据
     * @param code    失败代码
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> failed(@Nullable final T data, @Nullable final Integer code, @Nullable final String message) {
        final ResultVO<T> ret = ResultVO.ofFail();
        if (Objects.nonNull(data)) {
            ret.setData(data);
        }
        if (Objects.nonNull(code)) {
            ret.setCode(code);
        }
        if (!Strings.isNullOrEmpty(message)) {
            ret.setMessage(message);
        }
        return Mono.just(ret);
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
        return failed(null, code, message);
    }

    /**
     * 失败响应
     *
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> failed(@Nullable final String message) {
        return failed(null, null, message);
    }

    /**
     * 失败响应
     *
     * @param data 失败数据
     * @param <T>  数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> failed(@Nullable final T data) {
        return failed(data, null, null);
    }

    /**
     * 失败响应
     *
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> failed() {
        return failed((T) null);
    }

    /**
     * 响应失败
     *
     * @param e   失败异常
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> failed(@Nullable final EnumValue e) {
        if (Objects.isNull(e)) {
            return Mono.just(ResultVO.ofFail());
        }
        return Mono.just(ResultVO.of(e));
    }

    /**
     * 响应失败
     *
     * @param e   失败异常
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> Mono<ResultVO<T>> failed(@Nullable final Throwable e) {
        if (Objects.isNull(e)) {
            return failed();
        }
        return Mono.just(ResultVO.ofFail(e));
    }
}
