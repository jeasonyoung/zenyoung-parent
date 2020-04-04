package top.zenyoung.common.model;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 响应结果
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/6 4:39 下午
 **/
@Data
@Builder
@AllArgsConstructor
public class RespResult<T extends Serializable> implements Serializable {
    /**
     * 响应状态码
     */
    private Integer code;
    /**
     * 响应消息
     */
    private String msg;
    /**
     * 响应数据
     */
    private T data;

    public RespResult() {
        final EnumData ret = EnumData.parse(ResultCode.Success);
        this.code = ret.getVal();
        this.msg = ret.getTitle();
    }

    /**
     * 创建响应结果
     *
     * @param resultCode 响应结果枚举
     * @param msg        响应消息
     * @param data       响应数据
     */
    protected void buildRespResult(@Nonnull final ResultCode resultCode, @Nullable final String msg, @Nullable final T data) {
        setCode(resultCode.getVal());
        setMsg(Strings.isNullOrEmpty(msg) ? resultCode.getTitle() : msg);
        if (data != null) {
            setData(data);
        }
    }

    /**
     * 构建成功响应结果
     *
     * @param data 响应数据
     * @return 响应结果
     */
    public RespResult<T> buildRespSuccess(@Nullable final T data) {
        buildRespResult(ResultCode.Success, null, data);
        return this;
    }

    /**
     * 构建失败响应结果
     *
     * @param error 失败消息
     */
    public void buildRespFail(@Nullable final String error) {
        buildRespResult(ResultCode.Fail, error, null);
    }

    /**
     * 创建响应结果
     *
     * @param resultCode 响应结果枚举
     * @param msg        响应消息
     * @param data       响应数据
     * @param <T>        响应数据类型
     * @return 响应结果
     */
    protected static <T extends Serializable> RespResult<T> buildResult(@Nonnull final ResultCode resultCode, @Nullable final String msg, @Nullable final T data) {
        return RespResult.<T>builder()
                .code(resultCode.getVal())
                .msg(Strings.isNullOrEmpty(msg) ? resultCode.getTitle() : msg)
                .data(data)
                .build();
    }

    /**
     * 构建响应成功
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 响应结果
     */
    public static <T extends Serializable> RespResult<T> buildSuccess(@Nullable final T data) {
        return buildResult(ResultCode.Success, null, data);
    }

    /**
     * 构建响应失败
     *
     * @param error 响应错误消息
     * @param <T>   响应数据类型
     * @return 响应失败
     */
    public static <T extends Serializable> RespResult<T> buildFail(@Nullable final String error) {
        return buildResult(ResultCode.Fail, error, null);
    }
}
