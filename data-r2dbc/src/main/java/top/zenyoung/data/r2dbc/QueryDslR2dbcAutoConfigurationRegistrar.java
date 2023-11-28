package top.zenyoung.data.r2dbc;

import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import top.zenyoung.data.r2dbc.querydsl.QuerydslR2dbcRepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

class QueryDslR2dbcAutoConfigurationRegistrar extends AbstractRepositoryConfigurationSourceSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableR2dbcRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableR2dbcRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new QuerydslR2dbcRepositoryConfigurationExtension();
    }

    @EnableQuerydslR2dbcRepositories
    private static class EnableR2dbcRepositoriesConfiguration {

    }
}
