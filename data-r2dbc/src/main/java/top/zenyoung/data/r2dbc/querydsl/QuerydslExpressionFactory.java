package top.zenyoung.data.r2dbc.querydsl;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
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
        var fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, queryClass.getSimpleName().substring(1));
        var field = ReflectionUtils.findField(queryClass, fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Did not find a static field of the same type in " + queryClass);
        }
        final EntityPathBase<?> entity = (EntityPathBase<?>) ReflectionUtils.getField(field, null);
        if (entity == null) {
            throw new IllegalArgumentException(field.getName() + ",未继承 EntityPathBase");
        }
        final Class<?> entityClass = entity.getType();
        String schemaName = null, tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName());
        final AnnotatedElement annotatedElement;
        if (Objects.nonNull(annotatedElement = entity.getAnnotatedElement())) {
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
        return new RelationalPathBase<>(entityClass, entity.getMetadata(), schemaName, tableName);
    }
}
