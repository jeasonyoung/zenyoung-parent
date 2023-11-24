package top.zenyoung.jpa.reactive.querydsl.apt;

import com.google.auto.service.AutoService;
import com.querydsl.apt.AbstractQuerydslProcessor;
import com.querydsl.apt.Configuration;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import java.lang.annotation.Annotation;

@SupportedAnnotationTypes({
        "com.querydsl.core.annotations.*",
        "org.springframework.data.annotation.*",
        "org.springframework.data.relational.core.mapping.*"
})
@AutoService(Processor.class)
public class R2dbcAnnotationProcessor extends AbstractQuerydslProcessor {
    @Override
    protected Configuration createConfiguration(final RoundEnvironment roundEnv) {
        final Class<? extends Annotation> entity = Table.class;
        final Class<? extends Annotation> embedded = Embedded.class;
        final Class<? extends Annotation> skip = Transient.class;
        return new R2dbcConfiguration(roundEnv, processingEnv, entity, null, null, embedded, skip);
    }
}
