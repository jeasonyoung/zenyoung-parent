package top.zenyoung.data.r2dbc.querydsl;

import org.springframework.data.r2dbc.repository.config.R2dbcRepositoryConfigurationExtension;

import javax.annotation.Nonnull;

public class QuerydslR2dbcRepositoryConfigurationExtension extends R2dbcRepositoryConfigurationExtension {

    @Nonnull
    @Override
    public String getRepositoryFactoryBeanClassName() {
        return QuerydslR2dbcRepositoryFactoryBean.class.getName();
    }
}
