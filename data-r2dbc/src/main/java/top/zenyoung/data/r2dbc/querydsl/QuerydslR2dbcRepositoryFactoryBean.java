package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.sql.SQLQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.r2dbc.core.DatabaseClient;
import top.zenyoung.data.entity.Model;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * QuerydslR2dbc 数据访问工厂Bean
 *
 * @param <T> 数据访问接口
 * @param <M> 数据实体类型
 * @param <K> 主键类型
 * @author young
 */
public class QuerydslR2dbcRepositoryFactoryBean<T extends Repository<M, K>, M extends Model<K>, K extends Serializable>
        extends R2dbcRepositoryFactoryBean<T, M, K> {
    private SQLQueryFactory queryFactory;
    private DatabaseClient client;

    /**
     * 构造函数
     *
     * @param repositoryInterface 数据访问接口
     */
    protected QuerydslR2dbcRepositoryFactoryBean(@Nonnull final Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Nonnull
    @Override
    protected RepositoryFactorySupport getFactoryInstance(@Nonnull final R2dbcEntityOperations operations) {
        return new QuerydslR2dbcRepositoryFactory(operations, queryFactory, client);
    }

    @Autowired
    public void setQueryFactory(@Nonnull final SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Autowired
    public void setClient(@Nonnull final DatabaseClient client) {
        super.setDatabaseClient(client);
        this.client = client;
    }
}
