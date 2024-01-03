package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.binding.BindMarkersFactoryResolver;
import top.zenyoung.data.r2dbc.querydsl.impl.QuerydslR2dbcFragmentImpl;
import top.zenyoung.data.r2dbc.querydsl.impl.QuerydslR2dbcPredicateExecutorImpl;

import javax.annotation.Nonnull;

/**
 * QuerydslR2dbcRepositoryFactory
 *
 * @author young
 */
public class QuerydslR2dbcRepositoryFactory extends R2dbcRepositoryFactory {
    private static final Class<?> REPOSITORY_TARGET_TYPE = QuerydslR2dbcFragment.class;
    private final SQLQueryFactory queryFactory;
    private final DatabaseClient client;
    private final R2dbcConverter converter;
    private final QuerydslExpressionFactory querydslExpressionFactory = QuerydslExpressionFactory.of(REPOSITORY_TARGET_TYPE);
    private final QuerydslParameterBinder querydslParameterBinder;

    public QuerydslR2dbcRepositoryFactory(@Nonnull final R2dbcEntityOperations operations,
                                          @Nonnull final SQLQueryFactory queryFactory,
                                          @Nonnull final DatabaseClient client) {
        super(operations);
        this.queryFactory = queryFactory;
        this.converter = operations.getConverter();
        this.client = client;
        this.querydslParameterBinder = QuerydslParameterBinder.of(BindMarkersFactoryResolver.resolve(client.getConnectionFactory()));
    }

    @Nonnull
    @Override
    protected RepositoryComposition.RepositoryFragments getRepositoryFragments(@Nonnull final RepositoryMetadata metadata) {
        var fragments = super.getRepositoryFragments(metadata);
        var repositoryInterface = metadata.getRepositoryInterface();
        if (!REPOSITORY_TARGET_TYPE.isAssignableFrom(repositoryInterface)) {
            return fragments;
        }
        var path = querydslExpressionFactory.getRelationalPathBaseFromQueryRepositoryClass(repositoryInterface);
        var type = metadata.getDomainType();
        var constructorExpression = querydslExpressionFactory.getConstructorExpression(type, path);
        var querydslR2dbcFragment = createQuerydslR2dbcFragment(path, constructorExpression);
        var querydslPredicateExecutor = createQuerydslPredicateExecutor(constructorExpression, path);
        return fragments.append(querydslR2dbcFragment).append(querydslPredicateExecutor);
    }

    private RepositoryFragment<Object> createQuerydslR2dbcFragment(@Nonnull final RelationalPath<?> path,
                                                                   @Nonnull final ConstructorExpression<?> constructor) {
        var queryDslFragment = instantiateClass(QuerydslR2dbcFragmentImpl.class,
                queryFactory, constructor, path, client, converter, querydslParameterBinder);
        return RepositoryFragment.implemented(queryDslFragment);
    }

    private RepositoryFragment<Object> createQuerydslPredicateExecutor(@Nonnull final ConstructorExpression<?> constructorExpression,
                                                                       @Nonnull final RelationalPathBase<?> path) {
        var context = converter.getMappingContext();
        @SuppressWarnings("unchecked")
        var entity = context.getRequiredPersistentEntity(constructorExpression.getType());
        var querydsl = Querydsl.of(queryFactory, entity);
        var querydslPredicateExecutor = instantiateClass(QuerydslR2dbcPredicateExecutorImpl.class,
                constructorExpression, path, queryFactory, querydsl, client, converter, querydslParameterBinder);
        return RepositoryFragment.implemented(querydslPredicateExecutor);
    }
}
