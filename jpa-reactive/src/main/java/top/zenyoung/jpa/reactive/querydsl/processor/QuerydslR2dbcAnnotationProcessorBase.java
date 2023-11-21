package top.zenyoung.jpa.reactive.querydsl.processor;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import com.querydsl.apt.AbstractQuerydslProcessor;
import com.querydsl.apt.Configuration;
import com.querydsl.apt.ExtendedTypeFactory;
import com.querydsl.apt.TypeElementHandler;
import com.querydsl.codegen.*;
import com.querydsl.codegen.utils.model.Type;
import com.querydsl.codegen.utils.model.TypeCategory;
import com.querydsl.sql.codegen.NamingStrategy;
import lombok.experimental.UtilityClass;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class QuerydslR2dbcAnnotationProcessorBase extends AbstractQuerydslProcessor {
    private RoundEnvironment roundEnv;
    private ExtendedTypeFactory typeFactory;
    private Configuration conf;
    private CaseFormat projectTableCaseFormat;
    private CaseFormat projectColumnCaseFormat;

    private final NamingStrategy namingStrategy;
    private final BiFunction<TypeMappings, QueryTypeFactory, TypeElementHandler> typeElementHandlerFactory;

    protected QuerydslR2dbcAnnotationProcessorBase(@Nonnull final Class<? extends NamingStrategy> namingStrategyClass) {
        try {
            this.namingStrategy = namingStrategyClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to create new instance of " + namingStrategyClass, e);
        }
        this.typeElementHandlerFactory = (typeMappings, queryTypeFactory) -> new TypeElementHandler(conf, typeFactory, typeMappings, queryTypeFactory);
    }


    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (Objects.isNull(this.projectTableCaseFormat)) {
            this.projectTableCaseFormat = CaseFormat.UPPER_CAMEL;
        }
        if (Objects.isNull(this.projectColumnCaseFormat)) {
            this.projectColumnCaseFormat = CaseFormat.UPPER_CAMEL;
        }
        return super.process(annotations, roundEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Id.class.getName());
    }

    @Override
    protected Configuration createConfiguration(final RoundEnvironment roundEnv) {
        final Class<? extends Annotation> entity = Id.class;
        this.roundEnv = roundEnv;
        final CodegenModule codegenModule = new CodegenModule();
        final JavaTypeMappings typeMappings = new JavaTypeMappings();
        codegenModule.bind(TypeMappings.class, typeMappings);
        codegenModule.bind(QueryTypeFactory.class, new QueryTypeFactoryImpl("Q", "", ""));
        this.conf = new QuerydslR2dbcConfiguration(roundEnv, processingEnv, projectColumnCaseFormat, entity,
                null, null, Embedded.class, Transient.class, typeMappings,
                codegenModule, namingStrategy);
        return this.conf;
    }

    @Override
    protected TypeElementHandler createElementHandler(@Nonnull final TypeMappings typeMappings,
                                                      @Nonnull final QueryTypeFactory queryTypeFactory) {
        return typeElementHandlerFactory.apply(typeMappings, queryTypeFactory);
    }

    @Override
    protected ExtendedTypeFactory createTypeFactory(final Set<Class<? extends Annotation>> entityAnnotations,
                                                    final TypeMappings typeMappings,
                                                    final QueryTypeFactory queryTypeFactory) {
        this.typeFactory = new CustomExtendedTypeFactory(this.processingEnv, entityAnnotations, typeMappings,
                queryTypeFactory, this.conf, this.processingEnv.getElementUtils(), this.projectTableCaseFormat);
        return this.typeFactory;
    }

    protected Set<TypeElement> collectElements() {
        final Set<TypeElement> entityElements = roundEnv.getElementsAnnotatedWith(conf.getEntitiesAnnotation())
                .stream()
                .map(Element::getEnclosingElement)
                .filter(TypeElement.class::isInstance)
                .map(el -> (TypeElement) el)
                .collect(Collectors.toSet());
        return entityElements.stream()
                .flatMap(this::getEntityElementWithEmbeddedEntities)
                .collect(Collectors.toSet());
    }

    private Stream<TypeElement> getEntityElementWithEmbeddedEntities(@Nonnull final TypeElement entityElement) {
        final Types types = processingEnv.getTypeUtils();
        final Set<TypeElement> embeddedElements = ElementFilter.fieldsIn(entityElement.getEnclosedElements())
                .stream()
                .filter(el -> Objects.nonNull(el.getAnnotation(conf.getEmbeddedAnnotation())))
                .map(el -> types.asElement(el.asType()))
                .filter(TypeElement.class::isInstance)
                .map(el -> (TypeElement) el)
                .collect(Collectors.toSet());
        return Stream.concat(Stream.of(entityElement), embeddedElements.stream());
    }

    private static class CustomExtendedTypeFactory extends ExtendedTypeFactory {
        public static final String IS_EMBEDDED_DATA_KEY = "isEmbedded";
        private final Configuration configuration;
        private final Elements elements;
        private final CaseFormat tableCaseFormat;
        private final Types types;

        public CustomExtendedTypeFactory(@Nonnull final ProcessingEnvironment env,
                                         @Nonnull final Set<Class<? extends Annotation>> annotations,
                                         @Nonnull final TypeMappings typeMappings,
                                         @Nonnull final QueryTypeFactory queryTypeFactory,
                                         @Nonnull final Configuration configuration,
                                         @Nonnull final Elements elements,
                                         @Nonnull final CaseFormat tableCaseFormat) {
            super(env, annotations, typeMappings, queryTypeFactory, configuration.getVariableNameFunction());
            this.types = env.getTypeUtils();
            this.configuration = configuration;
            this.elements = elements;
            this.tableCaseFormat = tableCaseFormat;
        }

        @Override
        public boolean isSimpleTypeEntity(@Nonnull final TypeElement typeElement, @Nonnull final Class<? extends Annotation> entityAnn) {
            return typeElement.getAnnotation(entityAnn) != null ||
                    typeElement.getEnclosedElements().stream().anyMatch(el -> el.getAnnotation(entityAnn) != null);
        }

        @Nullable
        @Override
        public EntityType getEntityType(final TypeMirror typeMirror, final boolean deep) {
            final Element element = types.asElement(typeMirror);
            final EntityType entityType = super.getEntityType(typeMirror, deep);
            if (Objects.nonNull(element) && Objects.nonNull(entityType)) {
                final List<Property> embeddedlessProperties = entityType.getProperties().stream()
                        .flatMap(property -> {
                            if (Embeddeds.isEmbedded(configuration, element, property)) {
                                final Type type = property.getType();
                                if (!type.getCategory().equals(TypeCategory.ENTITY)) {
                                    return Stream.of(property);
                                }
                                return ((EntityType) type).getProperties().stream();
                            }
                            return Stream.of(property);
                        })
                        .collect(Collectors.toList());
                entityType.getProperties().clear();
                entityType.getProperties().addAll(embeddedlessProperties);
                entityType.getPropertyNames().clear();
                entityType.getPropertyNames().addAll(embeddedlessProperties.stream()
                        .map(Property::getName)
                        .collect(Collectors.toList())
                );
                updateModel(element, entityType);
            }
            return entityType;
        }

        private void updateModel(@Nonnull final Element element, @Nonnull final EntityType type) {
            final Map<Object, Object> data = type.getData();
            data.put("table", getTableName(type));
            type.getProperties().forEach(property -> {
                if (Embeddeds.isEmbedded(configuration, element, property)) {
                    property.getData().put(IS_EMBEDDED_DATA_KEY, true);
                }
            });
        }

        protected String getTableName(@Nonnull final EntityType model) {
            final String simpleName = model.getSimpleName(),
                    className = model.getPackageName() + "." + simpleName;
            final String tableName = CaseFormat.UPPER_CAMEL.to(tableCaseFormat, simpleName);
            return Optional.ofNullable(elements.getTypeElement(className).getAnnotation(Table.class))
                    .map(table -> {
                        if (table.value().isEmpty()) {
                            return table.name();
                        }
                        return table.value();
                    })
                    .orElse(tableName);
        }

    }

    @UtilityClass
    private static class Embeddeds {
        public static boolean isEmbedded(@Nonnull final Configuration configuration,
                                         @Nonnull final Element element,
                                         @Nonnull final Property property) {
            final Class<? extends Annotation> embeddedAnnotation = configuration.getEmbeddedAnnotation();
            if (Objects.isNull(embeddedAnnotation)) {
                return false;
            }
            return ElementFilter.fieldsIn(element.getEnclosedElements())
                    .stream()
                    .filter(el -> el.getSimpleName().toString().equals(property.getName()))
                    .allMatch(el -> Objects.nonNull(el.getAnnotation(embeddedAnnotation)));
        }

        public static boolean isEmbedded(@Nonnull final Configuration configuration, @Nonnull final Element element) {
            final Class<? extends Annotation> embeddedAnnotation = configuration.getEmbeddedAnnotation();
            if (Objects.isNull(embeddedAnnotation)) {
                return false;
            }
            return Objects.nonNull(element.getAnnotation(embeddedAnnotation));
        }
    }
}
