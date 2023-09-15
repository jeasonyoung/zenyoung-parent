package top.zenyoung.jpa.converter;

import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 * 枚举-类型转换器
 *
 * @param <T> 枚举类型
 * @author young
 */
@Converter
public abstract class BaseEnumConverter<T extends EnumValue> implements AttributeConverter<T, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final T attr) {
        return attr == null ? null : attr.getVal();
    }

    @Override
    public T convertToEntityAttribute(final Integer val) {
        if (Objects.nonNull(val)) {
            return parse(val);
        }
        return null;
    }

    /**
     * 枚举解析
     *
     * @param val 枚举值
     * @return 枚举对象
     */
    protected abstract T parse(@Nonnull final Integer val);
}
