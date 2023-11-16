package top.zenyoung.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Accessors(chain = true)
@Schema(description = "响应结果")
@AllArgsConstructor(staticName = "of")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultVO<T> implements Serializable {
    /**
     * 响应状态码
     */
    @Schema(description = "响应状态码")
    private Integer code;
    /**
     * 响应消息
     */
    @Schema(description = "响应消息")
    private String message;
    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
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
     * @param <T>       返回数据类型
     * @return 响应数据
     */
    public static <T> ResultVO<DataResult<T>> of(@Nonnull final EnumValue enumValue, @Nullable final DataResult<T> data) {
        final ResultVO<DataResult<T>> ret = of(enumValue);
        if (data != null) {
            ret.setData(data);
        }
        return ret;
    }

    /**
     * 成功响应数据
     *
     * @param data 业务数据
     * @param <T>  返回数据类型
     * @return 响应数据
     */
    public static <T> ResultVO<DataResult<T>> ofSuccess(@Nullable final DataResult<T> data) {
        return of(ResultCode.SUCCESS, data);
    }

    /**
     * 成功响应数据
     *
     * @param data 返回数据
     * @param <T>  返回数据类型
     * @return 响应数据
     */
    public static <T> ResultVO<T> ofSuccess(@Nullable final T data) {
        final ResultVO<T> ret = of(ResultCode.SUCCESS);
        if (Objects.nonNull(data)) {
            ret.setData(data);
        }
        return ret;
    }

    /**
     * 成功响应数据
     *
     * @param <T> 返回数据类型
     * @return 响应数据
     */
    public static <T> ResultVO<T> ofSuccess() {
        return ofSuccess((T) null);
    }

    /**
     * 成功响应数据
     *
     * @param e   异常对象
     * @param <T> 返回数据类型
     * @return 响应数据
     */
    public static <T> ResultVO<T> ofFail(@Nullable final Throwable e) {
        if (Objects.isNull(e)) {
            return ofFail();
        }
        String msg = e.getMessage();
        Throwable cause = e;
        while (Strings.isNullOrEmpty(msg) && Objects.nonNull(cause)) {
            cause = cause.getCause();
        }
        return ofFail(msg);
    }

    /**
     * 失败响应数据
     *
     * @param message 失败消息
     * @param <T>     返回数据类型
     * @return 响应数据
     */
    public static <T> ResultVO<T> ofFail(@Nullable final String message) {
        final ResultVO<T> ret = of(ResultCode.FAIL);
        if (!Strings.isNullOrEmpty(message)) {
            ret.setMessage(message);
        }
        return ret;
    }

    /**
     * 失败响应数据
     *
     * @param <T> 响应数据类型
     * @return 响应数据
     */
    public static <T> ResultVO<T> ofFail() {
        return ofFail((String) null);
    }
}
