package top.zenyoung.orm.enums;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.orm.annotation.PoField;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 实体字段枚举
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PoConstant {
    /**
     * 创建用户字段
     */
    CreateBy("createBy"),
    /**
     * 创建时间字段
     */
    CreateAt("createAt"),
    /**
     * 更新用户字段
     */
    UpdateBy("updateBy"),
    /**
     * 更新时间字段
     */
    UpdateAt("updateAt"),
    /**
     * 状态字段(0:禁用,1:启用)
     */
    Status("status"),
    /**
     * 逻辑删除字段(0:未删除,1:逻辑删除)
     */
    LogicDel("logicDel");

    /**
     * 字段名称
     */
    private final String fieldName;

    /**
     * 根据字段名转换
     *
     * @param fieldName 字段名
     * @return 实体字段枚举
     */
    public static PoConstant byFieldName(@Nullable final String fieldName) {
        if (!Strings.isNullOrEmpty(fieldName)) {
            for (PoConstant p : PoConstant.values()) {
                if (fieldName.equalsIgnoreCase(p.getFieldName())) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * 根据实体类字段注解转换
     *
     * @param poField 实体类字段注解
     * @return 实体字段枚举
     */
    public static PoConstant byPoField(@Nullable final PoField poField) {
        if (Objects.nonNull(poField)) {
            switch (poField.fill()) {
                case CreateBy:
                    return PoConstant.CreateBy;
                case CreateAt:
                    return PoConstant.CreateAt;
                case UpdateBy:
                    return PoConstant.UpdateBy;
                case UpdateAt:
                    return PoConstant.UpdateAt;
                case Status:
                    return PoConstant.Status;
                default:
                    return null;
            }
        }
        return null;
    }
}