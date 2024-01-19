package top.zenyoung.data.r2dbc.querydsl;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Querydsl 表达式工厂
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class QuerydslExpressionFactory {
    private static final Map<Class<?>, RelationalPath<?>> CLASS_RELATIONAL_PATH_CACHE = Maps.newHashMap();
    private static final Map<EntityPath<?>, RelationalPath<?>> ENTITY_RELATIONAL_PATH_CACHE = Maps.newHashMap();
    private static final Map<String, Map<String, String>> TABLE_FIELD_COLUMN_CACHE = Maps.newHashMap();
    //
    private final Class<?> repositoryTargetType;

    public QBean<?> getBeanExpression(@Nonnull final Class<?> type, @Nonnull final RelationalPath<?> pathBase) {
        final List<Path<?>> cols = Optional.ofNullable(pathBase.getColumns()).orElse(Lists.newArrayList());
        return Projections.bean(type, cols.toArray(Expression[]::new));
    }

    public RelationalPath<?> getRelationalPathBaseFromQueryRepositoryClass(@Nonnull final Class<?> repositoryInterface) {
        var entityType = ResolvableType.forClass(repositoryInterface)
                .as(repositoryTargetType)
                .getGeneric(0)
                .resolve();
        if (entityType == null) {
            throw new IllegalArgumentException("Could not resolve query class for " + repositoryInterface);
        }
        return getRelationalPathBaseFromQueryClass(getQueryClass(entityType));
    }

    private Class<?> getQueryClass(@Nonnull final Class<?> entityType) {
        var fullName = entityType.getPackage().getName() + ".Q" + entityType.getSimpleName();
        try {
            return entityType.getClassLoader().loadClass(fullName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to load class " + fullName);
        }
    }

    private RelationalPath<?> getRelationalPathBaseFromQueryClass(@Nonnull final Class<?> queryClass) {
        return CLASS_RELATIONAL_PATH_CACHE.computeIfAbsent(queryClass, key -> {
            var fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, key.getSimpleName().substring(1));
            var field = ReflectionUtils.findField(key, fieldName);
            if (field == null) {
                throw new IllegalArgumentException("Did not find a static field of the same type in " + key);
            }
            final EntityPathBase<?> entity = (EntityPathBase<?>) ReflectionUtils.getField(field, null);
            if (entity == null) {
                throw new IllegalArgumentException(field.getName() + ",未继承 EntityPathBase");
            }
            return fromEntityPath(entity);
        });
    }

    public static RelationalPath<?> fromEntityPath(@Nonnull final EntityPath<?> entity) {
        return ENTITY_RELATIONAL_PATH_CACHE.computeIfAbsent(entity, key -> {
            final Class<?> cls = key.getType();
            String schemaName = null, tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, cls.getSimpleName());
            final Table table = AnnotationUtils.findAnnotation(cls, Table.class);
            if (Objects.nonNull(table)) {
                schemaName = table.schema();
                final String name = Optional.of(table.value())
                        .filter(val -> !Strings.isNullOrEmpty(val))
                        .orElse(table.name());
                if (!Strings.isNullOrEmpty(name)) {
                    tableName = name;
                }
            }
            final var inner = new RelationalPathBaseInner<>(cls, key.getMetadata(), schemaName, tableName);
            inner.parseColumns(key);
            return inner;
        });
    }

    public static String getTableColumn(@Nonnull final RelationalPath<?> relational, @Nonnull final String fieldName) {
        return Optional.ofNullable(relational.getTableName())
                .filter(table -> !Strings.isNullOrEmpty(table))
                .map(table -> TABLE_FIELD_COLUMN_CACHE.getOrDefault(table, null))
                .map(tableColumnMap -> tableColumnMap.getOrDefault(fieldName, null))
                .orElse(null);
    }

    public static String getTableColumn(@Nonnull final EntityPath<?> entity, @Nonnull final String fieldName) {
        return getTableColumn(fromEntityPath(entity), fieldName);
    }

    private static class RelationalPathBaseInner<T> extends RelationalPathBase<T> {
        public RelationalPathBaseInner(final Class<? extends T> type, final PathMetadata metadata, final String schema, final String table) {
            super(type, metadata, schema, table);
        }

        public void parseColumns(@Nonnull final EntityPath<?> entity) {
            final Class<?> domainCls = super.getType(), entityCls = entity.getClass();
            final Field[] entityFields = entityCls.getDeclaredFields();
            for (final Field entityField : entityFields) {
                final Method method = ReflectionUtils.findMethod(entityField.getType(), "getMetadata");
                if (Objects.nonNull(method)) {
                    final var entityFieldObj = ReflectionUtils.getField(entityField, entity);
                    final var metadata = ReflectionUtils.invokeMethod(method, entityFieldObj);
                    if ((metadata instanceof PathMetadata pm) && pm.getPathType() == PathType.PROPERTY
                            && (pm.getElement() instanceof String property) && !Strings.isNullOrEmpty(property)) {
                        final Field field = ReflectionUtils.findField(domainCls, property);
                        if (Objects.nonNull(field)) {
                            final var entityFieldVal = ReflectionUtils.getField(entityField, entity);
                            if (entityFieldVal instanceof Path<?> entityFieldPath) {
                                final String fieldName = field.getName();
                                //检查字段名
                                String colName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
                                final Column column = AnnotationUtils.findAnnotation(field, Column.class);
                                if (Objects.nonNull(column) && !Strings.isNullOrEmpty(column.value())) {
                                    colName = column.value();
                                }
                                final String tableName;
                                if (!Strings.isNullOrEmpty(tableName = super.getTableName()) && !Strings.isNullOrEmpty(colName)) {
                                    final var fieldColumnMap = TABLE_FIELD_COLUMN_CACHE.computeIfAbsent(tableName, k -> Maps.newHashMap());
                                    fieldColumnMap.put(fieldName, colName);
                                }
                                //检查是否为主键
                                if (Objects.nonNull(AnnotationUtils.findAnnotation(field, Id.class))) {
                                    createPrimaryKey(entityFieldPath);
                                }
                                //添加字段
                                addMetadata(entityFieldPath, ColumnMetadata.getColumnMetadata(entityFieldPath));
                            }
                        }
                    }
                }
            }
        }
    }
}
