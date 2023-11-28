package top.zenyoung.data.mybatis.handler;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 枚举类型处理器
 *
 * @param <E> 枚举类型
 */
public class EnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {
    private static final Map<String, String> TABLE_METHOD_OF_ENUM_TYPES = Maps.newConcurrentMap();
    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();
    private final Class<E> enumClassType;
    private final Class<?> propertyType;
    private final Invoker getInvoker;

    public EnumTypeHandler(@Nonnull final Class<E> enumClassType) {
        this.enumClassType = enumClassType;
        final MetaClass metaClass = MetaClass.forClass(enumClassType, REFLECTOR_FACTORY);
        final String name = findEnumValueFieldName(this.enumClassType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find @JsonValue in base EnumValue Class: %s.", this.enumClassType.getName())));
        this.propertyType = ReflectionKit.resolvePrimitiveIfNecessary(metaClass.getGetterType(name));
        this.getInvoker = metaClass.getGetInvoker(name);
    }

    public Optional<String> findEnumValueFieldName(@Nonnull final Class<?> clazz) {
        if (clazz.isEnum()) {
            String className = clazz.getName();
            return Optional.ofNullable(CollectionUtils.computeIfAbsent(TABLE_METHOD_OF_ENUM_TYPES, className,
                    key -> {
                        Optional<Field> opt = findEnumValueField(clazz);
                        return opt.map(Field::getName).orElse(null);
                    }));
        }
        return Optional.empty();
    }

    private Optional<Field> findEnumValueField(@Nonnull final Class<?> clazz) {
        if (top.zenyoung.common.model.EnumValue.class.isAssignableFrom(clazz)) {
            return Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(JsonValue.class))
                    .findFirst();
        }
        return Optional.empty();
    }

    @Override
    public void setNonNullParameter(final PreparedStatement ps, final int i, final E parameter, final JdbcType jdbcType) throws SQLException {
        if (jdbcType == null) {
            ps.setObject(i, this.getValue(parameter));
        } else {
            // see r3589
            ps.setObject(i, this.getValue(parameter), jdbcType.TYPE_CODE);
        }
    }

    @Override
    public E getNullableResult(final ResultSet rs, final String columnName) throws SQLException {
        final Object value = rs.getObject(columnName, this.propertyType);
        if (null == value && rs.wasNull()) {
            return null;
        }
        return this.valueOf(value);
    }

    @Override
    public E getNullableResult(final ResultSet rs, final int columnIndex) throws SQLException {
        final Object value = rs.getObject(columnIndex, this.propertyType);
        if (null == value && rs.wasNull()) {
            return null;
        }
        return this.valueOf(value);
    }

    @Override
    public E getNullableResult(final CallableStatement cs, final int columnIndex) throws SQLException {
        final Object value = cs.getObject(columnIndex, this.propertyType);
        if (null == value && cs.wasNull()) {
            return null;
        }
        return this.valueOf(value);
    }

    protected boolean equalsValue(final Object sourceValue, final Object targetValue) {
        final String sValue = StringUtils.toStringTrim(sourceValue), tValue = StringUtils.toStringTrim(targetValue);
        if (sourceValue instanceof Number && targetValue instanceof Number && new BigDecimal(sValue).compareTo(new BigDecimal(tValue)) == 0) {
            return true;
        }
        return Objects.equals(sValue, tValue);
    }

    private E valueOf(final Object value) {
        final E[] es = this.enumClassType.getEnumConstants();
        return Arrays.stream(es)
                .filter(e -> equalsValue(value, getValue(e)))
                .findAny()
                .orElse(null);
    }

    private Object getValue(final Object object) {
        try {
            return this.getInvoker.invoke(object, new Object[0]);
        } catch (ReflectiveOperationException e) {
            throw ExceptionUtils.mpe(e);
        }
    }
}
