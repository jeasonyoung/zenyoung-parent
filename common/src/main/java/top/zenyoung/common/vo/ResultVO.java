package top.zenyoung.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.common.model.ResultCode;
import top.zenyoung.common.paging.DataResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Restful风格接口响应数据
 *
 * @author young
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultVO<T> implements Serializable {
    /**
     * 响应状态码
     */
    private Integer code;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;

    /**
     * 使用枚举构造响应数据
     *
     * @param enumValue 枚举接口
     * @param <T>       返回数据类型
     * @return 返回数据
     */
    public static <T> ResultVO<T> of(@Nonnull final EnumValue enumValue) {
        return ResultVO.<T>builder()
                .code(enumValue.getVal())
                .message(enumValue.getTitle())
                .build();
    }

    /**
     * 构建数据响应数据
     *
     * @param enumValue 枚举接口
     * @param data      数据对象
     * @param <R>       数据类型
     * @return 响应数据
     */
    public static <R extends Serializable> ResultVO<DataResult<R>> of(@Nonnull final EnumValue enumValue, @Nullable final DataResult<R> data) {
        final ResultVO<DataResult<R>> ret = of(enumValue);
        if (data != null) {
            ret.setData(data);
        }
        return ret;
    }

    /**
     * 成功响应数据
     *
     * @param data 业务数据
     * @param <R>  数据类型
     * @return 响应数据
     */
    public static <R extends Serializable> ResultVO<DataResult<R>> ofSuccess(@Nullable final DataResult<R> data) {
        return of(ResultCode.Success, data);
    }

    /**
     * 成功响应数据
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return 响应数据
     */
    public static <T> ResultVO<T> ofSuccess(@Nullable final T data) {
        final ResultVO<T> ret = of(ResultCode.Success);
        if (Objects.nonNull(data)) {
            ret.setData(data);
        }
        return ret;
    }

    /**
     * 失败响应数据
     *
     * @param message 失败消息
     * @return 响应数据
     */
    public static <T> ResultVO<T> ofFail(@Nullable final String message) {
        final ResultVO<T> ret = of(ResultCode.Fail);
        if (!Strings.isNullOrEmpty(message)) {
            ret.setMessage(message);
        }
        return ret;
    }
}
