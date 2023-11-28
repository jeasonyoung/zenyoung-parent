package top.zenyoung.data.r2dbc.querydsl;

import com.google.common.base.CaseFormat;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.RelationalPath;
import lombok.RequiredArgsConstructor;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mapping.Parameter;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.Pair;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class QuerydslR2dbcExpressionFactory {
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private final Class<?> repositoryTargetType;

    public ConstructorExpression<?> getConstructorExpression(@Nonnull final Class<?> type, @Nonnull final RelationalPath<?> path) {
        final Constructor<?> constructor = getConstructor(type);
        Assert.notNull(constructor, "Could not discover preferred constructor for " + type);
        final Map<String, Expression<?>> columnNameToExpression = path.getColumns().stream()
                .collect(Collectors.toMap(col -> col.getMetadata().getName(), Function.identity()));
        final Map<String, Expression<?>> embeddedConstructorParameterNameToPath = Arrays.stream(constructor.getParameters())
                .filter(parameter -> !columnNameToExpression.containsKey(parameter.getName()))
                .map(parameter -> Stream.of(type.getDeclaredFields())
                        .filter(field -> field.getName().equals(parameter.getName()))
                        .filter(field -> field.isAnnotationPresent(Embedded.class))
                        .<Class<?>>map(Field::getType)
                        .findAny()
                        .map(embeddedType -> getConstructorExpression(embeddedType, path))
                        .map(p -> Pair.of(parameter.getName(), p))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (o, n) -> n));
        final List<Pair<Class<?>, Expression<?>>> pairs = Stream.of(constructor.getParameters())
                .map(parameter -> {
                    final Expression<?> p = columnNameToExpression.getOrDefault(parameter.getName(), null);
                    if (Objects.isNull(p)) {
                        final String name = parameter.getName();
                        if (embeddedConstructorParameterNameToPath.containsKey(name)) {
                            final Expression<?> pe = embeddedConstructorParameterNameToPath.get(name);
                            return Pair.<Class<?>, Expression<?>>of(parameter.getType(), pe);
                        }
                        final Field field = ReflectionUtils.findField(type, name);
                        if (Objects.nonNull(field) && Objects.nonNull(AnnotationUtils.getAnnotation(field, MappedCollection.class))) {
                            final Class<?> collectionType = parameter.getType();
                            if (Set.class.isAssignableFrom(collectionType)) {
                                final ResolvableType resolvableType = ResolvableType.forType(parameter.getParameterizedType())
                                        .as(Set.class)
                                        .getGeneric(0);
                                final Class<?> target = Objects.requireNonNull(resolvableType.resolve());
                                final Expression<?> qClass = getRelationalPathFromQueryClass(getQueryClass(target));
                                return Pair.<Class<?>, Expression<?>>of(collectionType, new QSet(qClass));
                            }
                            throw new IllegalArgumentException("Unsupported collection type " + collectionType);
                        }
                        throw new IllegalArgumentException("Failed to match parameter " + name + " to QClass column for " + type);
                    }
                    return Pair.<Class<?>, Expression<?>>of(parameter.getType(), p);
                })
                .collect(Collectors.toList());
        final Class<?>[] paramTypes = pairs.stream().map(Pair::getFirst).toArray(Class[]::new);
        final Expression<?>[] expressions = pairs.stream().map(Pair::getSecond).toArray(Expression[]::new);
        return Projections.constructor(type, paramTypes, expressions);
    }

    private Constructor<?> getConstructor(@Nonnull final Class<?> type) {
        @SuppressWarnings({"unchecked", "rawtypes"}) final BiFunction<Constructor<?>, TypeInformation<?>, PreferredConstructor<?, ?>> buildPreferredConstructor = (
                constructor, typeInformation
        ) -> {
            if (constructor.getParameterCount() == 0) {
                return new PreferredConstructor<>(constructor);
            }
            final List<TypeInformation<?>> parameterTypes = typeInformation.getParameterTypes(constructor);
            final String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(constructor);
            final Parameter<?, ?>[] parameters = new Parameter[parameterTypes.size()];
            final Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            for (int i = 0; i < parameterTypes.size(); i++) {
                final String name = parameterNames == null || parameterNames.length <= i ? null : parameterNames[i];
                final TypeInformation<?> tinfo = parameterTypes.get(i);
                final Annotation[] annotations = parameterAnnotations[i];
                parameters[i] = new Parameter<>(name, tinfo, annotations, null);
            }
            return new PreferredConstructor(constructor, parameters);
        };
        final TypeInformation<?> typeInfo = ClassTypeInformation.from(type);
        final Constructor<?>[] constructors = typeInfo.getType().getDeclaredConstructors();
        PreferredConstructor<?, ?> preferredConstructor = Arrays.stream(constructors)
                .filter(it -> !it.isSynthetic())
                .filter(it -> it.isAnnotationPresent(PersistenceCreator.class))
                .map(it -> buildPreferredConstructor.apply(it, typeInfo))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(preferredConstructor)) {
            preferredConstructor = Arrays.stream(constructors)
                    .filter(it -> !it.isSynthetic())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .map(it -> buildPreferredConstructor.apply(it, typeInfo))
                    .orElse(null);
        }
        if (preferredConstructor == null) {
            return null;
        }
        return preferredConstructor.getConstructor();
    }


    public RelationalPath<?> getRelationalPathFromQueryRepositoryClass(@Nonnull final Class<?> repositoryInterface) {
        final Class<?> entityType = ResolvableType.forClass(repositoryTargetType)
                .as(repositoryTargetType)
                .getGeneric(0)
                .resolve();
        Assert.notNull(entityType, "Could not resolve query class for " + repositoryInterface);
        return getRelationalPathFromQueryClass(getQueryClass(entityType));
    }

    private Class<?> getQueryClass(@Nonnull final Class<?> entityType) {
        final String fullName = entityType.getPackage().getName() + ".Q" + entityType.getSimpleName();
        try {
            return entityType.getClassLoader().loadClass(fullName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to load class " + fullName);
        }
    }

    private RelationalPath<?> getRelationalPathFromQueryClass(@Nonnull final Class<?> queryClass) {
        final String fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, queryClass.getSimpleName().substring(1));
        final Field field = ReflectionUtils.findField(queryClass, fieldName);
        Assert.notNull(field, "Did not find a static field of the same type in " + queryClass);
        return (RelationalPath<?>) ReflectionUtils.getField(field, null);
    }
}
