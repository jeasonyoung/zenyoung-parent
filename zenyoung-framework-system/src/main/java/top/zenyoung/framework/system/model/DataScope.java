package top.zenyoung.framework.system.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nullable;

/**
 * 数据权限范围-枚举
 *
 * @author young
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DataScope implements EnumValue {
    /**
     * 未数据授权
     */
    None(0, "未数据授权"),
    /**
     * 全部数据权限
     */
    ALL(1, "全部数据权限"),
    /**
     * 自定数据权限
     */
    Custom(2, "自定数据权限"),
    /**
     * 本部门数据权限
     */
    OwnDept(3, "本部门数据权限"),
    /**
     * 部门及以下数据权限
     */
    DeptAndBelow(4, "部门及以下数据权限"),
    /**
     * 仅本人数据权限
     */
    OnlySelf(5, "仅本人数据权限");

    private final int val;
    private final String title;

    public static DataScope parse(@Nullable final Integer val) {
        if (val != null) {
            for (DataScope s : DataScope.values()) {
                if (val == s.getVal()) {
                    return s;
                }
            }
        }
        return DataScope.None;
    }
}
