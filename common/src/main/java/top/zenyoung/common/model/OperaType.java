package top.zenyoung.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;

/**
 * 业务操作类型
 *
 * @author young
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "业务操作类型", enumAsRef = true)
public enum OperaType implements EnumValue {
    /**
     * 查询
     */
    QUERY(0, "查询"),
    /**
     * 新增
     */
    ADD(1, "新增"),
    /**
     * 修改
     */
    MODIFY(2, "修改"),
    /**
     * 删除
     */
    DEL(3, "删除"),
    /**
     * 其它
     */
    OTHER(4, "其它");

    private final int val;
    private final String title;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
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
