package top.zenyoung.data.apt.jpa;

import com.querydsl.apt.AbstractQuerydslProcessor;
import com.querydsl.apt.Configuration;
import com.querydsl.apt.jpa.JPAConfiguration;
import jakarta.persistence.*;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import java.lang.annotation.Annotation;

@SupportedAnnotationTypes({
        "com.querydsl.core.annotations.*",
        "jakarta.persistence.*"
})
public class JPAAnnotationProcessor extends AbstractQuerydslProcessor {

    @Override
    protected Configuration createConfiguration(final RoundEnvironment roundEnv) {
        Class<? extends Annotation> entity = Entity.class;
        Class<? extends Annotation> superType = MappedSuperclass.class;
        Class<? extends Annotation> embeddable = Embeddable.class;
        Class<? extends Annotation> embedded = Embedded.class;
        Class<? extends Annotation> skip = Transient.class;
        return new JPAConfiguration(roundEnv, processingEnv,
                entity, superType, embeddable, embedded, skip);
    }
}
