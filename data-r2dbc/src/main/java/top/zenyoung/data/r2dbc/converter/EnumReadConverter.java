package top.zenyoung.data.r2dbc.converter;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.data.converter.EnumConverter;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * 枚举读取转换器
 *
 * @author young
 */
@Slf4j
@ReadingConverter
public class EnumReadConverter implements ConverterFactory<Integer, EnumValue> {
    private static final Map<Class<?>, EnumConverter<? extends EnumValue>> enumConverterMap = Maps.newConcurrentMap();

    @Nonnull
    @Override
    public <T extends EnumValue> Converter<Integer, T> getConverter(@Nonnull final Class<T> targetType) {
        final Convert convert = targetType.getAnnotation(Convert.class);
        if (Objects.isNull(convert)) {
            throw new ServiceException(targetType + ",未配置注解: @Convert.");
        }
        final Class<? extends EnumConverter<? extends EnumValue>> enumCls = convert.converter();
        if (Objects.isNull(enumCls)) {
            throw new ServiceException(targetType + ",未配置注解参数: @Convert(converter=XXX).");
        }
        final EnumConverter<? extends EnumValue> enumConverter = enumConverterMap.computeIfAbsent(enumCls, k -> {
            try {
                return enumCls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("getConverter(enumCls: {})-exp: {}", enumCls, e.getMessage());
                throw new ServiceException(targetType + ",注解参数初始化失败[" + enumCls + "]:" + e.getMessage());
            }
        });
        return source -> targetType.cast(enumConverter.convertToEntityAttribute(source));
    }
}
