package top.zenyoung.data.r2dbc.querydsl;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
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
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Querydsl 表达式工厂
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class QuerydslExpressionFactory {
    private static final Map<Class<?>, RelationalPathBase<?>> CLASS_RELATIONAL_PATH_CACHE = Maps.newHashMap();
    private static final Map<EntityPathBase<?>, RelationalPathBase<?>> ENTITY_RELATIONAL_PATH_CACHE = Maps.newHashMap();
    private static final Map<String, Map<String, String>> TABLE_FIELD_COLUMN_CACHE = Maps.newHashMap();
    //
    private final Class<?> repositoryTargetType;

    public ConstructorExpression<?> getConstructorExpression(@Nonnull final Class<?> type, @Nonnull final RelationalPath<?> pathBase) {
        var constructor = getConstructor(type);
        if (constructor == null) {
            throw new IllegalArgumentException("Could not discover preferred constructor for " + type);
        }
        final Map<String, Expression<?>> columnNameToExpression = pathBase.getColumns()
                .stream()
                .collect(Collectors.toMap(column -> column.getMetadata().getName(), Function.identity()));
        var parameters = constructor.getParameters();
        var embeddedConstructorParameterNameToPath = getEmbeddedConstructorParameterNameToPath(
                type,
                pathBase,
                columnNameToExpression,
                parameters);
        var pairs = Stream.of(constructor.getParameters())
                .map(parameter -> getExpression(type, columnNameToExpression, embeddedConstructorParameterNameToPath, parameter))
                .toList();
        final Class<?>[] paramTypes = pairs.stream().map(ParameterAndExpressionPair::getParameterType).toArray(Class[]::new);
        final Expression<?>[] expressions = pairs.stream().map(ParameterAndExpressionPair::getExpression).toArray(Expression[]::new);
        return Projections.constructor(type, paramTypes, expressions);
    }

    private ParameterAndExpressionPair getExpression(final Class<?> type, final Map<String, Expression<?>> columnNameToPath,
                                                     final Map<String, Expression<?>> embeddedConstructorParameterNameToPath,
                                                     final Parameter parameter) {
        var path = columnNameToPath.get(parameter.getName());
        if (Objects.isNull(path)) {
            return resolveNonColumnParameter(type, embeddedConstructorParameterNameToPath, parameter);
        }
        return ParameterAndExpressionPair.of(parameter.getType(), path);
    }

    private ParameterAndExpressionPair resolveNonColumnParameter(@Nonnull final Class<?> type,
                                                                 @Nonnull final Map<String, Expression<?>> embeddedConstructorParameterNameToPath,
                                                                 @Nonnull final Parameter parameter) {
        var name = parameter.getName();
        if (embeddedConstructorParameterNameToPath.containsKey(name)) {
            return ParameterAndExpressionPair.of(parameter.getType(), embeddedConstructorParameterNameToPath.get(name));
        }
        var field = ReflectionUtils.findField(type, name);
        if (Objects.nonNull(field) && Objects.nonNull(AnnotationUtils.getAnnotation(field, MappedCollection.class))) {
            return resolveMappedCollectionParameter(parameter);
        }
        throw new IllegalArgumentException("Failed to match parameter " + name + " to QClass column for " + type);
    }

    private ParameterAndExpressionPair resolveMappedCollectionParameter(@Nonnull final Parameter parameter) {
        var collectionType = parameter.getType();
        if (Set.class.isAssignableFrom(collectionType)) {
            var resolvableType = ResolvableType.forType(parameter.getParameterizedType()).as(Set.class).getGeneric(0);
            var target = Objects.requireNonNull(resolvableType.resolve());
            final Expression<?> qClass = getRelationalPathBaseFromQueryClass(getQueryClass(target));
            return ParameterAndExpressionPair.of(collectionType, new QSet(qClass));
        }
        throw new IllegalArgumentException("Unsupported collection type " + collectionType);
    }

    private Map<String, Expression<?>> getEmbeddedConstructorParameterNameToPath(@Nonnull final Class<?> type,
                                                                                 @Nonnull final RelationalPath<?> pathBase,
                                                                                 @Nonnull final Map<String, Expression<?>> columnNameToColumn,
                                                                                 @Nonnull final Parameter[] parameters) {
        final Map<String, Expression<?>> embeddedConstructorParameterNameToPath = Maps.newHashMap();
        Stream.of(parameters)
                .filter(parameter -> !columnNameToColumn.containsKey(parameter.getName()))
                .forEach(parameter -> getEmbeddedType(type, parameter)
                        .map(embeddedType -> getConstructorExpression(embeddedType, pathBase))
                        .ifPresent(path -> embeddedConstructorParameterNameToPath.put(parameter.getName(), path)));
        return embeddedConstructorParameterNameToPath;
    }

    private Optional<Class<?>> getEmbeddedType(@Nonnull final Class<?> type, @Nonnull final Parameter parameter) {
        return Stream.of(type.getDeclaredFields())
                .filter(field -> field.getName().equals(parameter.getName()))
                .filter(field -> field.isAnnotationPresent(Embedded.class))
                .<Class<?>>map(Field::getType)
                .findAny();
    }

    private Constructor<?> getConstructor(@Nonnull final Class<?> type) {
        final PreferredConstructor<?, ?> preferredConstructor = PreferredConstructorDiscoverer.discover(type);
        if (preferredConstructor == null) {
            return null;
        }
        return preferredConstructor.getConstructor();
    }

    public RelationalPathBase<?> getRelationalPathBaseFromQueryRepositoryClass(@Nonnull final Class<?> repositoryInterface) {
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

    private RelationalPathBase<?> getRelationalPathBaseFromQueryClass(@Nonnull final Class<?> queryClass) {
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

    public static RelationalPathBase<?> fromEntityPath(@Nonnull final EntityPathBase<?> entity) {
        return ENTITY_RELATIONAL_PATH_CACHE.computeIfAbsent(entity, key -> {
            final Class<?> cls = key.getType();
            String schemaName = null, tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, cls.getSimpleName());
            final AnnotatedElement annotatedElement;
            if (Objects.nonNull(annotatedElement = key.getAnnotatedElement())) {
                final Table table = annotatedElement.getAnnotation(Table.class);
                if (Objects.nonNull(table)) {
                    schemaName = table.schema();
                    final String name = Optional.of(table.value())
                            .filter(val -> !Strings.isNullOrEmpty(val))
                            .orElse(table.name());
                    if (!Strings.isNullOrEmpty(name)) {
                        tableName = name;
                    }
                }
            }
            final var inner = new RelationalPathBaseInner<>(cls, key.getMetadata(), schemaName, tableName);
            inner.parseColumns(key);
            return inner;
        });
    }

    public static String getTableColumn(@Nonnull final RelationalPathBase<?> relational, @Nonnull final String fieldName) {
        return Optional.ofNullable(relational.getTableName())
                .filter(table -> !Strings.isNullOrEmpty(table))
                .map(table -> TABLE_FIELD_COLUMN_CACHE.getOrDefault(table, null))
                .map(tableColumnMap -> tableColumnMap.getOrDefault(fieldName, null))
                .orElse(null);
    }

    public static String getTableColumn(@Nonnull final EntityPathBase<?> entity, @Nonnull final String fieldName) {
        return getTableColumn(fromEntityPath(entity), fieldName);
    }

    private static class RelationalPathBaseInner<T> extends RelationalPathBase<T> {
        public RelationalPathBaseInner(final Class<? extends T> type, final PathMetadata metadata, final String schema, final String table) {
            super(type, metadata, schema, table);
        }

        public void parseColumns(@Nonnull final EntityPathBase<?> entity) {
            final Class<?> domainCls = super.getType(), entityCls = entity.getClass();
            final Field[] fields = entityCls.getDeclaredFields();
            for (final Field field : fields) {
                final Method method = ReflectionUtils.findMethod(field.getType(), "getMetadata");
                if (Objects.nonNull(method)) {
                    final var fieldObj = ReflectionUtils.getField(field, entity);
                    final var invokeRet = ReflectionUtils.invokeMethod(method, fieldObj);
                    if ((invokeRet instanceof PathMetadata pm)
                            && pm.getPathType() == PathType.PROPERTY
                            && (pm.getElement() instanceof String property)
                            && !Strings.isNullOrEmpty(property)) {
                        final Field col = ReflectionUtils.findField(domainCls, property);
                        if (Objects.nonNull(col)) {
                            final var colProp = ReflectionUtils.getField(field, entity);
                            if (colProp instanceof Path<?> colPropPath) {
                                //检查字段名
                                String colName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, col.getName());
                                final Column column = col.getAnnotation(Column.class);
                                if (Objects.nonNull(column) && !Strings.isNullOrEmpty(column.value())) {
                                    colName = column.value();
                                }
                                final String tableName;
                                if (!Strings.isNullOrEmpty(tableName = super.getTableName()) && !Strings.isNullOrEmpty(colName)) {
                                    final var fieldColumnMap = TABLE_FIELD_COLUMN_CACHE.computeIfAbsent(tableName, k -> Maps.newHashMap());
                                    fieldColumnMap.put(col.getName(), colName);
                                }
                                //检查是否为主键
                                final var idAnnon = col.getAnnotation(Id.class);
                                if (Objects.nonNull(idAnnon)) {
                                    createPrimaryKey(colPropPath);
                                }
                                //添加字段
                                addMetadata(colPropPath, ColumnMetadata.getColumnMetadata(colPropPath));
                            }
                        }
                    }
                }
            }
        }
    }
}
