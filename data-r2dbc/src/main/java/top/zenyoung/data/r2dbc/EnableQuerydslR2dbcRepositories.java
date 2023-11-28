package top.zenyoung.data.r2dbc;

import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import top.zenyoung.data.r2dbc.querydsl.QuerydslR2dbcRepositoryFactoryBean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableR2dbcRepositories(repositoryFactoryBeanClass = QuerydslR2dbcRepositoryFactoryBean.class)
public @interface EnableQuerydslR2dbcRepositories {

}
