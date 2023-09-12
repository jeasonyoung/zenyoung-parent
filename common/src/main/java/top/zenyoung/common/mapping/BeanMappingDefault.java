package top.zenyoung.common.mapping;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.internal.Errors;
import org.modelmapper.internal.util.Primitives;
import org.modelmapper.spi.ConditionalConverter;
import org.modelmapper.spi.MappingContext;
import top.zenyoung.common.paging.PageList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bean转换-默认实现
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanMappingDefault implements BeanMapping {
    public static final BeanMappingDefault INSTANCE = new BeanMappingDefault();
    private static final ModelMapper MODEL;

    static {
        MODEL = new ModelMapper();
        final Configuration configuration = MODEL.getConfiguration();
        configuration.setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setDeepCopyEnabled(true)
                .setFullTypeMatchingRequired(true);
        final List<ConditionalConverter<?, ?>> converters = configuration.getConverters();
        if (converters != null && !converters.isEmpty()) {
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
    public <T, R> R mapping(@Nullable final T data, @Nonnull final Class<R> cls) {
        return Optional.ofNullable(data)
                .map(item -> MODEL.map(item, cls))
                .orElse(null);
    }

    @Override
    public <T, R> List<R> mapping(@Nullable final List<T> items, @Nonnull final Class<R> cls) {
        if (Objects.nonNull(items)) {
            return items.stream()
                    .filter(Objects::nonNull)
                    .map(item -> mapping(item, cls))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public <T extends Serializable, R extends Serializable> PageList<R> mapping(@Nullable final PageList<T> pageList,
                                                                                @Nonnull final Class<R> cls) {
        if (Objects.nonNull(pageList)) {
            return new PageList<R>() {
                @Override
                public Long getTotal() {
                    return pageList.getTotal();
                }

                @Override
                public List<R> getRows() {
                    return mapping(pageList.getRows(), cls);
                }
            };
        }
        return PageList.empty();
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
                return numberFor((boolean) source ? 1 : 0, destType);
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
