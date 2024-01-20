package top.zenyoung.data.r2dbc.converter;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.common.util.CalcUtils;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 枚举读取转换器
 *
 * @author young
 */
@Slf4j
@ReadingConverter
public class EnumReadConverter implements ConverterFactory<Number, EnumValue> {
    private static final Map<Class<?>, Map<Integer, ? extends EnumValue>> CLASS_ENUMS_CACHE = Maps.newConcurrentMap();

    @Nonnull
    @Override
    public <T extends EnumValue> Converter<Number, T> getConverter(@Nonnull final Class<T> targetType) {
        return val -> {
            if (targetType.isEnum()) {
                if (!EnumValue.class.isAssignableFrom(targetType)) {
                    throw new ServiceException(targetType + ",枚举未继承: EnumValue 接口.");
                }
                final Map<Integer, ? extends EnumValue> valEnumMaps = CLASS_ENUMS_CACHE.computeIfAbsent(targetType, k -> {
                    final T[] enums = targetType.getEnumConstants();
                    if (enums != null && enums.length > 0) {
                        return CalcUtils.map(Sets.newHashSet(enums), EnumValue::getVal, Function.identity());
                    }
                    return Maps.newHashMap();
                });
                if (!CollectionUtils.isEmpty(valEnumMaps)) {
                    final EnumValue ev = valEnumMaps.getOrDefault(val.intValue(), null);
                    if (Objects.nonNull(ev)) {
                        return targetType.cast(ev);
                    }
                }
            }
            return null;
        };
    }
}
