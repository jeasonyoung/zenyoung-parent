package top.zenyoung.jpa.reactive.querydsl.processor;


import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;

@AutoService(Processor.class)
public class QuerydslR2dbcAnnotationProcessor extends QuerydslR2dbcAnnotationProcessorBase {

    public QuerydslR2dbcAnnotationProcessor() {
        super(QuerydslR2dbcNamingStrategy.class);
    }
}
