package top.zenyoung.boot.service.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.internal.Errors;
import org.modelmapper.internal.util.Primitives;
import org.modelmapper.spi.ConditionalConverter;
import org.modelmapper.spi.MappingContext;
import org.springframework.util.CollectionUtils;
import top.zenyoung.boot.service.BeanMappingService;
import top.zenyoung.common.paging.PageList;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Bean对象转换服务接口实现
 *
 * @author young
 */
@Slf4j
public class BeanMappingServiceImpl implements BeanMappingService {
    private static final ModelMapper MODEL = new ModelMapper();

    static {
        final Configuration configuration = MODEL.getConfiguration();
        configuration.setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setDeepCopyEnabled(true)
                .setFullTypeMatchingRequired(true);
        final List<ConditionalConverter<?, ?>> converters = configuration.getConverters();
        if (!CollectionUtils.isEmpty(converters)) {
            ConditionalConverter<?, ?> remove = null;
            for (ConditionalConverter<?, ?> converter : converters) {
                if (converter.getClass().getSimpleName().contains("NumberConverter")) {
                    remove = converter;
                    break;
                }
            }
            if (Objects.nonNull(remove)) {
                converters.remove(remove);
                converters.add(new NumberConverter());
            }
        }
    }

    @Override
    public <T, MT> MT mapping(final T data, final Class<MT> mtClass) {
        if (Objects.isNull(data)) {
            return null;
        }
        return MODEL.map(data, mtClass);
    }

    @Override
    public <T, MT> List<MT> mapping(final List<T> items, final Class<MT> mtClass) {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        return items.stream()
                .map(item -> mapping(item, mtClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public <T, MT> PageList<MT> mapping(final PageList<T> pageList, final Class<MT> mtClass) {
        if (Objects.isNull(pageList)) {
            return null;
        }
        return new PageList<MT>() {
            @Override
            public Long getTotal() {
                return pageList.getTotal();
            }

            @Override
            public List<MT> getRows() {
                return mapping(pageList.getRows(), mtClass);
            }
        };
    }

    private static class NumberConverter implements ConditionalConverter<Object, Number> {

        @Override
        public Number convert(final MappingContext<Object, Number> context) {
            final Object source = context.getSource();
            if (Objects.isNull(source)) {
                return null;
            }
            final Class<?> destType = Primitives.wrapperFor(context.getDestinationType());
            if (source instanceof Number) {
                return numberFor((Number) source, destType);
            }
            if (source instanceof Boolean) {
                return numberFor((Boolean) source ? 1 : 0, destType);
            }
            if (source instanceof Date && Long.class.equals(destType)) {
                return ((Date) source).getTime();
            }
            if (source instanceof Calendar && Long.class.equals(destType)) {
                return ((Calendar) source).getTime().getTime();
            }
            if (source instanceof XMLGregorianCalendar && Long.class.equals(destType)) {
                return ((XMLGregorianCalendar) source).toGregorianCalendar().getTimeInMillis();
            }
            return numberFor(source.toString(), destType);
        }

        @Override
        public MatchResult match(final Class<?> sourceType, final Class<?> destType) {
            final boolean destMatch = Number.class.isAssignableFrom(Primitives.wrapperFor(destType));
            if (destMatch) {
                return Number.class.isAssignableFrom(Primitives.wrapperFor(sourceType))
                        || sourceType == Boolean.class || sourceType == Boolean.TYPE
                        || sourceType == String.class || Date.class.isAssignableFrom(sourceType)
                        || Calendar.class.isAssignableFrom(sourceType)
                        || XMLGregorianCalendar.class.isAssignableFrom(sourceType) ? MatchResult.FULL : MatchResult.PARTIAL;
            } else {
                return MatchResult.NONE;
            }
        }

        private Number numberFor(final Number source, final Class<?> destType) {
            if (destType.equals(source.getClass())) {
                return source;
            }
            if (destType.equals(Byte.class)) {
                final long val = source.longValue();
                if (val > Byte.MAX_VALUE) {
                    throw new Errors().errorTooLarge(source, destType).toMappingException();
                }
                if (val < Byte.MIN_VALUE) {
                    throw new Errors().errorTooSmall(source, destType).toMappingException();
                }
                return source.byteValue();
            }
            if (destType.equals(Short.class)) {
                final long val = source.longValue();
                if (val > Short.MAX_VALUE) {
                    throw new Errors().errorTooLarge(source, destType).toMappingException();
                }
                if (val < Short.MIN_VALUE) {
                    throw new Errors().errorTooSmall(source, destType).toMappingException();
                }
                return source.shortValue();
            }
            if (destType.equals(Integer.class)) {
                long longValue = source.longValue();
                if (longValue > Integer.MAX_VALUE) {
                    throw new Errors().errorTooLarge(source, destType).toMappingException();
                }
                if (longValue < Integer.MIN_VALUE) {
                    throw new Errors().errorTooSmall(source, destType).toMappingException();
                }
                return source.intValue();
            }

            if (destType.equals(Long.class)) {
                return source.longValue();
            }

            if (destType.equals(Float.class)) {
                if (source.doubleValue() > Float.MAX_VALUE) {
                    throw new Errors().errorTooLarge(source, destType).toMappingException();
                }
                return source.floatValue();
            }

            if (destType.equals(Double.class)) {
                return source.doubleValue();
            }

            if (destType.equals(BigDecimal.class)) {
                if (source instanceof Float || source instanceof Double) {
                    return new BigDecimal(source.toString());
                } else if (source instanceof BigInteger) {
                    return new BigDecimal((BigInteger) source);
                } else {
                    return BigDecimal.valueOf(source.longValue());
                }
            }
            if (destType.equals(BigInteger.class)) {
                if (source instanceof BigDecimal) {
                    return ((BigDecimal) source).toBigInteger();
                } else {
                    return BigInteger.valueOf(source.longValue());
                }
            }
            throw new Errors().errorMapping(source, destType).toMappingException();
        }

        Number numberFor(final String source, final Class<?> destType) {
            final String val = source.trim();
            if (Strings.isNullOrEmpty(val)) {
                return null;
            }
            try {
                if (destType.equals(Byte.class)) {
                    return Byte.valueOf(source);
                }
                if (destType.equals(Short.class)) {
                    return Short.valueOf(source);
                }
                if (destType.equals(Integer.class)) {
                    return Integer.valueOf(source);
                }
                if (destType.equals(Long.class)) {
                    return Long.valueOf(source);
                }
                if (destType.equals(Float.class)) {
                    return Float.valueOf(source);
                }
                if (destType.equals(Double.class)) {
                    return Double.valueOf(source);
                }
                if (destType.equals(BigDecimal.class)) {
                    return new BigDecimal(source);
                }
                if (destType.equals(BigInteger.class)) {
                    return new BigInteger(source);
                }
            } catch (Exception e) {
                return null;
            }
            throw new Errors().errorMapping(source, destType).toMappingException();
        }
    }
}
