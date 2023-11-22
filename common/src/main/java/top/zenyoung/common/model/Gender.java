package top.zenyoung.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;

/**
 * 性别-枚举
 *
 * @author young
 */
@Getter
@Schema(description = "性别", enumAsRef = true)
@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Gender implements EnumValue {
    /**
     * 未知
     */
    NONE(0, "未知"),
    /**
     * 男
     */
    MALE(1, "男"),
    /**
     * 女
     */
    FEMALE(2, "女");

    private final int val;
    private final String title;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Gender parse(@Nullable final Integer val) {
        if (val != null) {
            for (Gender g : Gender.values()) {
                if (g.getVal() == val) {
                    return g;
                }
            }
        }
        return Gender.NONE;
    }
}
