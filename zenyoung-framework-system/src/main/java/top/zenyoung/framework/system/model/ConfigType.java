package top.zenyoung.framework.system.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nullable;

/**
 * 配置类型
 *
 * @author young
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ConfigType implements EnumValue {
    /**
     * 系统内置
     */
    System(0, "系统内置"),
    /**
     * 自定义
     */
    Custom(1, "自定义");

    private final int val;
    private final String title;

    @JsonCreator
    public static ConfigType parse(@Nullable final Integer val) {
        if (val != null) {
            for (ConfigType t : ConfigType.values()) {
                if (t.getVal() == val) {
                    return t;
                }
            }
        }
        return null;
    }
}
