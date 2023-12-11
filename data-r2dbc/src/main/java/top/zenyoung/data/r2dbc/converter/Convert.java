package top.zenyoung.data.r2dbc.converter;

import top.zenyoung.common.model.EnumValue;
import top.zenyoung.data.converter.EnumConverter;

import java.lang.annotation.*;

/**
 * 实体数据转换
 *
 * @author yangyong
 */
@Inherited
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Convert {
    /**
     * 枚举类型转换
     *
     * @return 枚举转换类型
     */
    Class<? extends EnumConverter<? extends EnumValue>> converter();
}
