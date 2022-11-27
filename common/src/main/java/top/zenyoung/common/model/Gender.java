package top.zenyoung.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.zenyoung.annotation.DbEnumValue;

import javax.annotation.Nullable;

/**
 * 性别
 *
 * @author young
 */
@Getter
@ApiModel("性别")
@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Gender implements EnumValue {
    /**
     * 未知
     */
    None(0, "未知"),
    /**
     * 男
     */
    Male(1, "男"),
    /**
     * 女
     */
    Female(2, "女");

    @JsonValue
    @DbEnumValue
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
        return Gender.None;
    }
}
