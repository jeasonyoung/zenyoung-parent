package top.zenyoung.web.vo;

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

    /**
     * 构造函数
     */
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
        final EnumData ret = EnumData.parse(resultCode);
        setCode(ret.getVal());
        setMsg(Strings.isNullOrEmpty(msg) ? ret.getTitle() : msg);
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
     * 静态构建响应结果
     *
     * @param code 响应状态码
     * @param msg  响应消息
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 响应结果
     */
    public static <T extends Serializable> RespResult<T> of(@Nullable final Integer code, @Nullable final String msg, @Nullable final T data) {
        return RespResult.<T>builder()
                .code(code)
                .msg(msg)
                .data(data)
                .build();
    }

    /**
     * 静态构建响应结果
     *
     * @param resultCode 响应结果枚举
     * @param msg        响应消息
     * @param data       响应数据
     * @param <T>        响应数据类型
     * @return 响应结果
     */
    public static <T extends Serializable> RespResult<T> of(@Nonnull final ResultCode resultCode, @Nullable final String msg, @Nullable final T data) {
        final EnumData ret = EnumData.parse(resultCode);
        return of(ret.getVal(), Strings.isNullOrEmpty(msg) ? ret.getTitle() : msg, data);
    }

    /**
     * 静态构建成功响应结果
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 响应结果
     */
    public static <T extends Serializable> RespResult<T> ofSuccess(@Nullable final T data) {
        return RespResult.of(ResultCode.Success, null, data);
    }

    /**
     * 静态构建失败响应结果
     *
     * @param err 失败消息
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public static <T extends Serializable> RespResult<T> ofFail(@Nullable final String err) {
        return RespResult.of(ResultCode.Fail, err, null);
    }
}
