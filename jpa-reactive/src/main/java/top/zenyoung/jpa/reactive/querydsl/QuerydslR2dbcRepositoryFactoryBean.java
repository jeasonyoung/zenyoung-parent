package top.zenyoung.jpa.reactive.querydsl;

import com.querydsl.r2dbc.mysql.MySqlR2dbcQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.annotation.Nonnull;
import java.io.Serializable;

public class QuerydslR2dbcRepositoryFactoryBean<T extends Repository<S, K>, S, K extends Serializable>
        extends R2dbcRepositoryFactoryBean<T, S, K> {
    private MySqlR2dbcQueryFactory queryFactory;

    @Autowired
    public void setQueryFactory(@Nonnull final MySqlR2dbcQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Autowired
    public void setEntityTemplate(@Nonnull final R2dbcEntityTemplate entityTemplate) {
        super.setEntityOperations(entityTemplate);
    }

    public QuerydslR2dbcRepositoryFactoryBean(@Nonnull final Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Nonnull
    @Override
    protected RepositoryFactorySupport getFactoryInstance(@Nonnull final R2dbcEntityOperations operations) {
        return new QuerydslR2dbcRepositoryFactory(operations, queryFactory);
    }
}
