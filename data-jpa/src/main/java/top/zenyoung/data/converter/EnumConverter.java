package top.zenyoung.data.converter;

import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 枚举数据转换
 *
 * @param <T> 枚举类型
 * @author young
 */
public interface EnumConverter<T extends EnumValue> {
    /**
     * 将枚举对象转换为数据列值
     *
     * @param attr 枚举对象
     * @return 数据列值
     */
    default Integer convertToDatabaseColumn(@Nullable final T attr) {
        return Objects.isNull(attr) ? null : attr.getVal();
    }

    /**
     * 将数据列值转换为枚举对象
     *
     * @param val 数据列值
     * @return 枚举对象
     */
    T convertToEntityAttribute(@Nullable final Integer val);
}
