package top.zenyoung.jpa.reactive.querydsl.processor;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.querydsl.apt.APTOptions;
import com.querydsl.apt.DefaultConfiguration;
import com.querydsl.apt.VisitorConfig;
import com.querydsl.codegen.*;
import com.querydsl.codegen.utils.CodeWriter;
import com.querydsl.core.annotations.QueryEntities;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.codegen.MetaDataSerializer;
import com.querydsl.sql.codegen.NamingStrategy;
import com.querydsl.sql.codegen.SQLCodegenModule;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.inject.Named;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QuerydslR2dbcConfiguration extends DefaultConfiguration {
    private final SQLCodegenModule sqlCodegenModule;

    public QuerydslR2dbcConfiguration(@Nonnull final RoundEnvironment roundEnv,
                                      @Nonnull final ProcessingEnvironment processingEnv,
                                      @Nonnull final CaseFormat columnCaseFormat,
                                      @Nonnull final Class<? extends Annotation> entityAnn,
                                      @Nullable final Class<? extends Annotation> superTypeAnn,
                                      @Nullable final Class<? extends Annotation> embeddableAnn,
                                      @Nullable final Class<? extends Annotation> embeddedAnn,
                                      @Nullable final Class<? extends Annotation> skipAnn,
                                      @Nonnull final TypeMappings typeMappings,
                                      @Nonnull final CodegenModule codegenModule,
                                      @Nonnull final NamingStrategy namingStrategy) {
        super(processingEnv, roundEnv, processingEnv.getOptions(), Keywords.JPA, QueryEntities.class, entityAnn,
                superTypeAnn, embeddableAnn, embeddedAnn, skipAnn, codegenModule);
        super.setStrictMode(true);
        this.sqlCodegenModule = new SQLCodegenModule();
        final Class<? extends Annotation> generatedAnnotationClass = GeneratedAnnotationResolver.resolve(
                processingEnv.getOptions().get(APTOptions.QUERYDSL_GENERATED_ANNOTATION_CLASS)
        );
        this.sqlCodegenModule.bindInstance(CodegenModule.GENERATED_ANNOTATION_CLASS, generatedAnnotationClass);
        this.sqlCodegenModule.bind(NamingStrategy.class, namingStrategy);
        this.sqlCodegenModule.bind(TypeMappings.class, typeMappings);
        this.sqlCodegenModule.bind(ProcessingEnvironment.class, processingEnv);
        this.sqlCodegenModule.bind(CaseFormat.class, columnCaseFormat);
        this.sqlCodegenModule.bind(Serializer.class, CustomMetaDataSerializer.class);
    }

    @Override
    public boolean isBlockedField(@Nonnull final VariableElement field) {
        if (field.getAnnotation(MappedCollection.class) != null) {
            return true;
        }
        return super.isBlockedField(field);
    }

    @Override
    public VisitorConfig getConfig(@Nonnull final TypeElement e, @Nonnull final List<? extends Element> elements) {
        return VisitorConfig.FIELDS_ONLY;
    }

    @Override
    public Serializer getEntitySerializer() {
        return this.sqlCodegenModule.get(Serializer.class);
    }

    @Override
    public SerializerConfig getSerializerConfig(@Nonnull final EntityType entityType) {
        return SimpleSerializerConfig.DEFAULT;
    }

    public static class CustomMetaDataSerializer extends MetaDataSerializer {
        private final ProcessingEnvironment processingEnvironment;
        private final CaseFormat columnCaseFormat;

        @Inject
        public CustomMetaDataSerializer(@Nonnull final TypeMappings typeMappings,
                                        @Nonnull final NamingStrategy namingStrategy,
                                        @Nonnull final ProcessingEnvironment processingEnvironment,
                                        @Nonnull final CaseFormat columnCaseFormat,
                                        @Named(SQLCodegenModule.INNER_CLASSES_FOR_KEYS) boolean innerClassesForKeys,
                                        @Named(SQLCodegenModule.IMPORTS) Set<String> imports,
                                        @Named(SQLCodegenModule.COLUMN_COMPARATOR) Comparator<Property> columnComparator,
                                        @Named(SQLCodegenModule.ENTITYPATH_TYPE) Class<?> entityPathType,
                                        @Named(SQLCodegenModule.GENERATED_ANNOTATION_CLASS) Class<? extends Annotation> generatedAnnotationClass) {
            super(typeMappings, namingStrategy, innerClassesForKeys, imports, columnComparator, entityPathType, generatedAnnotationClass);
            this.processingEnvironment = processingEnvironment;
            this.columnCaseFormat = columnCaseFormat;
        }

        @Override
        protected void serializeProperties(@Nonnull final EntityType model, @Nonnull final SerializerConfig config,
                                           @Nonnull final CodeWriter writer) throws IOException {
            final CustomPropertiesEntityType newModel = withFieldOrderedProperties(model);
            super.serializeProperties(newModel, config, writer);
        }

        @Override
        protected void outro(@Nonnull final EntityType model, @Nonnull final CodeWriter writer) throws IOException {
            final CustomPropertiesEntityType newModel = withFieldOrderedProperties(model);
            super.outro(newModel, writer);
        }

        private CustomPropertiesEntityType withFieldOrderedProperties(@Nonnull final EntityType entityType) {
            final Set<Property> properties = entityType.getProperties();
            final Map<String, Property> propertyNameToProperty = properties.stream()
                    .collect(Collectors.toMap(Property::getName, Function.identity(), (o, n) -> n));
            final TypeElement typeElement = this.processingEnvironment.getElementUtils().getTypeElement(entityType.getFullName());
            final Set<Property> orderedProperties = ElementFilter.fieldsIn(Lists.newArrayList(typeElement.getEnclosedElements()))
                    .stream()
                    .map(field -> propertyNameToProperty.get(field.getSimpleName().toString()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            properties.stream()
                    .filter(property -> !orderedProperties.contains(property))
                    .forEach(orderedProperties::add);
            //
            final List<Property> orderedPropertiesList = Lists.newArrayList(orderedProperties);
            for (int i = 0; i < orderedPropertiesList.size(); i++) {
                final Property property = orderedPropertiesList.get(i);
                property.getData().put("COLUMN", ColumnMetadata.named(getColumnName(property)).withIndex(i));
            }
            //
            return new CustomPropertiesEntityType(entityType, orderedProperties);
        }

        protected String getColumnName(@Nonnull final Property property) {
            final TypeElement parentType = processingEnvironment.getElementUtils()
                    .getTypeElement(property.getDeclaringType().getFullName());
            return parentType.getEnclosedElements()
                    .stream()
                    .filter(VariableElement.class::isInstance)
                    .map(el -> (VariableElement) el)
                    .filter(el -> el.getSimpleName().toString().equals(property.getName()))
                    .filter(el -> el.getAnnotation(Column.class) != null)
                    .map(el -> el.getAnnotation(Column.class).value())
                    .findAny()
                    .orElseGet(() -> CaseFormat.LOWER_CAMEL.to(columnCaseFormat, property.getName()));

        }
    }

    @EqualsAndHashCode(callSuper = true)
    private static class CustomPropertiesEntityType extends EntityType {
        private final Set<Property> fieldOrderedProperties;

        CustomPropertiesEntityType(@Nonnull final EntityType entityType, @Nonnull final Set<Property> fieldOrderedProperties) {
            super(entityType);
            this.fieldOrderedProperties = fieldOrderedProperties;
        }

        @Override
        public Set<Property> getProperties() {
            return this.fieldOrderedProperties;
        }
    }
}
