package top.zenyoung.framework.system.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nullable;

/**
 * 菜单类型-枚举
 *
 * @author young
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MenuType implements EnumValue {
    /**
     * 目录
     */
    Dir(1, "目录"),
    /**
     * 菜单
     */
    Menu(2, "菜单"),
    /**
     * 按钮
     */
    Button(3, "按钮");

    private final int val;
    private final String title;

    public static MenuType parse(@Nullable final Integer val) {
        if (val != null) {
            for (MenuType t : MenuType.values()) {
                if (val == t.getVal()) {
                    return t;
                }
            }
        }
        return null;
    }
}
