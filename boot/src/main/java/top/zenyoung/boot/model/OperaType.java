package top.zenyoung.boot.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nullable;

/**
 * 业务操作记录-数据实体
 *
 * @author young
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum OperaType implements EnumValue {
    /**
     * 查询
     */
    Query(0, "查询"),
    /**
     * 新增
     */
    Add(1, "新增"),
    /**
     * 修改
     */
    Modify(2, "修改"),
    /**
     * 删除
     */
    Del(3, "删除"),
    /**
     * 其它
     */
    Other(4, "其它");

    private final int val;
    private final String title;

    public static OperaType parse(@Nullable final Integer val) {
        if (val != null) {
            for (OperaType t : OperaType.values()) {
                if (val == t.getVal()) {
                    return t;
                }
            }
        }
        return null;
    }
}
