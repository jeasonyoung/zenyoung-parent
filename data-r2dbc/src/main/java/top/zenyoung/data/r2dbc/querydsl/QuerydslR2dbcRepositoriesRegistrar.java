package top.zenyoung.data.r2dbc.querydsl;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;

public class QuerydslR2dbcRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    @Nonnull
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableQuerydslR2dbcRepositories.class;
    }

    @Nonnull
    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new QuerydslR2dbcRepositoryConfigurationExtension();
    }
}
