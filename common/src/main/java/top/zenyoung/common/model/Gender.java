package top.zenyoung.common.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;

/**
 * 性别-枚举
 *
 * @author young
 */
@Getter
@ToString
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

    @com.baomidou.mybatisplus.annotation.EnumValue
    private final int val;
    private final String title;

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
