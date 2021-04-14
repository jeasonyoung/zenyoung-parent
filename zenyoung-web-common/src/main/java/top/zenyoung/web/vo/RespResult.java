package top.zenyoung.web.vo;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.zenyoung.common.model.EnumData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 响应结果
 *
 * @author yangyong
 * @version 1.0
 **/
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    protected RespResult() {
        buildResp(ResultCode.Success, null);
    }

    /**
     * 构建响应数据
     *
     * @param resultCode 结果枚举类型
     * @param msg        响应消息
     */
    public void buildResp(@Nonnull final ResultCode resultCode, @Nullable final String msg) {
        final EnumData ret = EnumData.parse(resultCode);
        //响应状态码
        this.code = ret.getVal();
        //响应消息
        this.msg = Strings.isNullOrEmpty(msg) ? ret.getTitle() : msg;
    }

    /**
     * 构建响应数据
     *
     * @param resultCode 结果枚举类型
     * @param msg        响应消息
     * @param data       响应数据
     */
    public void buildResp(@Nonnull final ResultCode resultCode, @Nullable final String msg, @Nullable final T data) {
        buildResp(resultCode, msg);
        if (data != null) {
            this.data = data;
        }
    }

    /**
     * 构建响应消息
     *
     * @param code 响应状态码
     * @param msg  响应消息
     */
    public void buildResp(@Nullable final Integer code, @Nullable final String msg) {
        if (code != null) {
            this.code = code;
        }
        if (!Strings.isNullOrEmpty(msg)) {
            this.msg = msg;
        }
    }

    /**
     * 构建响应消息
     *
     * @param code 响应状态码
     * @param msg  响应消息
     * @param data 响应数据
     */
    public void buildResp(@Nullable final Integer code, @Nullable final String msg, @Nullable final T data) {
        buildResp(code, msg);
        if (data != null) {
            this.data = data;
        }
    }

    /**
     * 构建成功响应结果
     *
     * @param data 响应数据
     * @return 响应结果
     */
    public RespResult<T> buildRespSuccess(@Nullable final T data) {
        //创建响应结果
        buildResp(ResultCode.Success, null);
        //响应数据
        this.data = data;
        //返回对象
        return this;
    }

    /**
     * 构建失败响应结果
     *
     * @param error 失败消息
     */
    public void buildRespFail(@Nullable final String error) {
        //创建响应结果
        buildResp(ResultCode.Fail, null);
        //响应消息
        if (!Strings.isNullOrEmpty(error)) {
            this.msg = error;
        }
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
        return new RespResult<>(code, msg, data);
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
