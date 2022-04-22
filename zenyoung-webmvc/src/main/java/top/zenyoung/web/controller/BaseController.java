package top.zenyoung.web.controller;

import com.google.common.base.Strings;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.web.vo.ResultVO;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

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
     * @param data    业务数据
     * @param code    响应代码
     * @param message 响应信息
     * @param <T>     业务数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> success(@Nullable final T data, @Nullable final Integer code, @Nullable final String message) {
        final ResultVO<T> ret = ResultVO.ofSuccess(data);
        if (code != null) {
            ret.setCode(code);
        }
        if (!Strings.isNullOrEmpty(message)) {
            ret.setMessage(message);
        }
        return ret;
    }

    /**
     * 成功响应
     *
     * @param data    业务数据
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> success(@Nullable final T data, @Nullable final String message) {
        return success(data, null, message);
    }

    /**
     * 成功响应
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> success(@Nullable final T data) {
        return success(data, null, null);
    }

    /**
     * 成功响应
     *
     * @param items 业务数据集合
     * @param <T>   响应数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<List<T>> success(@Nullable final List<T> items) {
        return ResultVO.ofSuccess(items);
    }

    /**
     * 成功响应
     *
     * @param dataResult 业务数据
     * @param <T>        数据类型
     * @return 响应数据
     */
    protected <T extends Serializable> ResultVO<DataResult<T>> success(@Nullable final DataResult<T> dataResult) {
        return ResultVO.ofSuccess(dataResult);
    }

    /**
     * 成功响应
     *
     * @param pagingResult 分页数据
     * @param <T>          数据类型
     * @return 响应数据
     */
    protected <T extends Serializable> ResultVO<DataResult<T>> success(@Nullable final PagingResult<T> pagingResult) {
        if (pagingResult != null) {
            return success(DataResult.of(pagingResult));
        }
        return success((DataResult<T>) null);
    }

    /**
     * 成功响应
     *
     * @return 响应数据
     */
    protected <T> ResultVO<T> success() {
        return ResultVO.ofSuccess((T) null);
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
        return ResultVO.ofFail(null);
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


}
