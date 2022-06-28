package top.zenyoung.boot.controller;

import com.google.common.base.Strings;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 控制器-基类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/10 9:53 下午
 **/
public class BaseController {

    /**
     * 成功响应
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> success(@Nullable final T data) {
        return ResultVO.ofSuccess(data);
    }

    /**
     * 成功响应
     *
     * @param dataResult 业务数据
     * @param <T>        数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<PageList<T>> success(@Nullable final PageList<T> dataResult) {
        return ResultVO.ofSuccess(dataResult);
    }

    /**
     * 成功响应
     *
     * @return 响应数据
     */
    protected <T> ResultVO<T> success() {
        return success((T) null);
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
    protected <T> ResultVO<T> failed(@Nullable final T data, @Nullable final Integer code, @Nullable final String message) {
        final ResultVO<T> ret = ResultVO.ofFail(message);
        if (data != null) {
            ret.setData(data);
        }
        if (code != null) {
            ret.setCode(code);
        }
        return ret;
    }

    /**
     * 失败响应
     *
     * @param code    失败代码
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final Integer code, @Nullable final String message) {
        return failed(null, code, message);
    }

    /**
     * 失败响应
     *
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final String message) {
        return failed(null, null, message);
    }

    /**
     * 失败响应
     *
     * @param data 失败数据
     * @param <T>  数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final T data) {
        return failed(data, null, null);
    }

    /**
     * 失败响应
     *
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed() {
        return failed((T) null);
    }

    /**
     * 响应失败
     *
     * @param e   失败异常
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final EnumValue e) {
        final ResultVO<T> ret = failed();
        if (e != null) {
            ret.setCode(e.getVal());
            if (!Strings.isNullOrEmpty(e.getTitle())) {
                ret.setMessage(e.getTitle());
            }
        }
        return ret;
    }

    /**
     * 响应失败
     *
     * @param e   失败异常
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final Throwable e) {
        if (Objects.isNull(e)) {
            return failed();
        }
        if (e instanceof EnumValue) {
            return failed(((EnumValue) e));
        }
        return failed(e.getMessage());
    }
}
