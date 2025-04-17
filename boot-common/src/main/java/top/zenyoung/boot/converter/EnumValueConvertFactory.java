package top.zenyoung.boot.converter;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * 枚举值转换工厂
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class EnumValueConvertFactory implements ConverterFactory<String, EnumValue> {
    @Override
    @SuppressWarnings({"all"})
    public <T extends EnumValue> Converter<String, T> getConverter(@Nonnull final Class<T> targetType) {
        return source -> {
            if (!Strings.isNullOrEmpty(source)) {
                final T[] enums = targetType.getEnumConstants();
                if (Pattern.matches("\\d+", source)) {
                    final int val = Integer.parseInt(source);
                    for (final T e : enums) {
                        if (e.getVal() == val) {
                            return e;
                        }
                    }
                }
            }
            return null;
        };
    }
}
