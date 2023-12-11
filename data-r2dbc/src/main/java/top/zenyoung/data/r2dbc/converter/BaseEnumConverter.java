package top.zenyoung.data.r2dbc.converter;

import org.springframework.core.convert.converter.Converter;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.data.converter.EnumConverter;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * 枚举-类型转换器
 *
 * @param <T> 枚举类型
 * @author young
 */
public abstract class BaseEnumConverter<T extends EnumValue> implements EnumConverter<T> {

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
