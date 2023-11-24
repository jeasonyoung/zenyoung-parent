package top.zenyoung.jpa.reactive.querydsl.apt;

import com.google.common.base.CaseFormat;
import com.querydsl.apt.AbstractQuerydslProcessor;
import com.querydsl.apt.Configuration;
import com.querydsl.apt.ExtendedTypeFactory;
import com.querydsl.apt.TypeElementHandler;
import com.querydsl.codegen.*;
import com.querydsl.codegen.utils.model.Type;
import com.querydsl.codegen.utils.model.TypeCategory;
import com.querydsl.sql.codegen.NamingStrategy;
import lombok.experimental.UtilityClass;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedAnnotationTypes({
        "com.querydsl.core.annotations.*",
        "org.springframework.data.annotation.*",
        "org.springframework.data.relational.core.mapping.*"
})
public class R2dbcAnnotationProcessor extends AbstractQuerydslProcessor {
    private static final CaseFormat columnCaseFormat = CaseFormat.UPPER_CAMEL;
    private static final CaseFormat tableCaseFormat = CaseFormat.UPPER_CAMEL;
    private RoundEnvironment roundEnv;
    private Configuration conf;
    private ExtendedTypeFactory typeFactory;

    @Override
    protected Configuration createConfiguration(final RoundEnvironment roundEnv) {
        this.roundEnv = roundEnv;
        //
        final Class<? extends Annotation> entity = Table.class;
        final Class<? extends Annotation> embedded = Embedded.class;
        final Class<? extends Annotation> skip = Transient.class;
        //
        final CodegenModule codegenModule = new CodegenModule();
        final JavaTypeMappings typeMappings = new JavaTypeMappings();
        codegenModule.bind(TypeMappings.class, typeMappings);
        codegenModule.bind(QueryTypeFactory.class, new QueryTypeFactoryImpl("Q", "", ""));
        //
        final NamingStrategy namingStrategy = new R2dbcNamingStrategy();
        //
        this.conf = new R2dbcConfiguration(roundEnv, processingEnv, columnCaseFormat, entity,
                null, null, embedded, skip, typeMappings, codegenModule, namingStrategy);
        return this.conf;
    }

    @Override
    protected ExtendedTypeFactory createTypeFactory(final Set<Class<? extends Annotation>> entityAnnotations,
                                                    final TypeMappings typeMappings,
                                                    final QueryTypeFactory queryTypeFactory) {
        this.typeFactory = new CustomExtendedTypeFactory(processingEnv, entityAnnotations, typeMappings,
                queryTypeFactory, this.conf, processingEnv.getElementUtils(), tableCaseFormat);
        return this.typeFactory;
    }

    @Nonnull
    @Override
    protected TypeElementHandler createElementHandler(@Nonnull final TypeMappings typeMappings,
                                                      @Nonnull final QueryTypeFactory queryTypeFactory) {


        return new TypeElementHandler(this.conf, this.typeFactory, typeMappings, queryTypeFactory);
    }

    @Override
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
}
