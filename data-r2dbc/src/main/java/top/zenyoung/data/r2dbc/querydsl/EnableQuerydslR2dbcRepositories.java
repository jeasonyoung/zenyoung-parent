package top.zenyoung.data.r2dbc.querydsl;

import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable QuerydslR2dbcRepository support.
 *
 * @see EnableR2dbcRepositories
 */
@Import(R2dbcConfiguration.class)
@EnableR2dbcRepositories(repositoryFactoryBeanClass = QuerydslR2dbcRepositoryFactoryBean.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableQuerydslR2dbcRepositories {

}
