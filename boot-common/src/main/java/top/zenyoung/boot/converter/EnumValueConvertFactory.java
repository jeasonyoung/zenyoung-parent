package top.zenyoung.boot.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;

/**
 * 枚举值转换工厂
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class EnumValueConvertFactory implements ConverterFactory<Integer, EnumValue> {
    @Override
    @SuppressWarnings({"all"})
    public <T extends EnumValue> Converter<Integer, T> getConverter(@Nonnull final Class<T> targetType) {
        return source -> {
            final T[] enums = targetType.getEnumConstants();
            for (final T e : enums) {
                if (e.getVal() == source) {
                    return e;
                }
            }
            return null;
        };
    }
}
