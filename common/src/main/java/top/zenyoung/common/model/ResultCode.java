package top.zenyoung.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.annotation.DbEnumValue;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 结果状态
 *
 * @author young
 **/
@Getter
@ApiModel("结果状态")
@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResultCode implements EnumValue {
    /**
     * 成功
     */
    Success(0, "成功"),
    /**
     * 失败
     */
    Fail(-1, "失败"),
    /**
     * 错误
     */
    Error(500, "错误");

    @DbEnumValue
    private final int val;
    private final String title;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ResultCode parse(@Nullable final Integer val) {
        if (Objects.nonNull(val)) {
            for (final ResultCode code : ResultCode.values()) {
                if (code.val == val) {
                    return code;
                }
            }
        }
        return null;
    }
}
