package top.zenyoung.jpa.reactive.querydsl.apt;

import com.google.common.collect.Lists;
import com.querydsl.apt.DefaultConfiguration;
import com.querydsl.apt.TypeUtils;
import com.querydsl.apt.VisitorConfig;
import com.querydsl.codegen.Keywords;
import com.querydsl.core.annotations.QueryEntities;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class R2dbcConfiguration extends DefaultConfiguration {
    private final List<Class<? extends Annotation>> annotations;
    private final Types types;

    public R2dbcConfiguration(@Nonnull final RoundEnvironment roundEnv,
                              @Nonnull final ProcessingEnvironment processingEnv,
                              @Nonnull final Class<? extends Annotation> entityAnn,
                              @Nullable final Class<? extends Annotation> superTypeAnn,
                              @Nullable final Class<? extends Annotation> embeddableAnn,
                              @Nullable final Class<? extends Annotation> embeddedAnn,
                              @Nullable final Class<? extends Annotation> skipAnn) {
        super(processingEnv, roundEnv, Keywords.JPA, QueryEntities.class, entityAnn,
                superTypeAnn, embeddableAnn, embeddedAnn, skipAnn);
        this.annotations = getAnnotations();
        this.types = processingEnv.getTypeUtils();
        this.setStrictMode(true);
    }

    protected List<Class<? extends Annotation>> getAnnotations() {
        return Collections.unmodifiableList(Lists.newArrayList(
                AccessType.class, Id.class, Transient.class, Version.class,
                Column.class, Embedded.class, MappedCollection.class, Table.class
        ));
    }

    @Override
    public VisitorConfig getConfig(@Nonnull final TypeElement e, @Nonnull final List<? extends Element> elements) {
        final AccessType access = e.getAnnotation(AccessType.class);
        if (Objects.nonNull(access)) {
            if (access.value() == AccessType.Type.FIELD) {
                return VisitorConfig.FIELDS_ONLY;
            } else {
                return VisitorConfig.METHODS_ONLY;
            }
        }
        boolean fields = false, methods = false;
        for (Element element : elements) {
            if (hasRelevantAnnotation(element)) {
                fields |= element.getKind() == ElementKind.FIELD;
                methods |= element.getKind() == ElementKind.METHOD;
            }
        }
        return VisitorConfig.get(fields, methods, VisitorConfig.ALL);
    }

    @Override
    public TypeMirror getRealType(@Nonnull final ExecutableElement method) {
        return getRealElementType(method);
    }

    @Override
    public TypeMirror getRealType(@Nonnull final VariableElement field) {
        return getRealElementType(field);
    }

    @Nullable
    private TypeMirror getRealElementType(@Nonnull final Element element) {
        AnnotationMirror mirror = TypeUtils.getAnnotationMirrorOfType(element, MappedCollection.class);
        if (Objects.nonNull(mirror)) {
            final TypeMirror typeArg = TypeUtils.getAnnotationValueAsTypeMirror(mirror, "targetEntity");
            final TypeMirror erasure = (element instanceof ExecutableElement) ? ((ExecutableElement) element).getReturnType() :
                    types.erasure(element.asType());
            final TypeElement typeElement = (TypeElement) types.asElement(erasure);
            if (typeElement != null && typeArg != null) {
                if (typeElement.getTypeParameters().size() == 1) {
                    return types.getDeclaredType(typeElement, typeArg);
                } else if (typeElement.getTypeParameters().size() == 2 && (element.asType() instanceof DeclaredType)) {
                    final TypeMirror first = ((DeclaredType) element.asType()).getTypeArguments().get(0);
                    return types.getDeclaredType(typeElement, first, typeArg);
                }
            }
        }
        return null;
    }

    private boolean hasRelevantAnnotation(@Nonnull final Element element) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (element.getAnnotation(annotation) != null) {
                return true;
            }
        }
        return false;
    }
}
