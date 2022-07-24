package top.zenyoung.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

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

    @JsonValue
    @com.baomidou.mybatisplus.annotation.EnumValue
    private final int val;
    private final String title;

    @JsonCreator
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
