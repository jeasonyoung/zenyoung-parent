package top.zenyoung.jpa.reactive.querydsl;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQueryFactory;
import com.querydsl.sql.RelationalPath;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;

import javax.annotation.Nonnull;

public class QuerydslR2dbcRepositoryFactory extends R2dbcRepositoryFactory {
    private static final Class<?> REPOSITORY_TARGET_TYPE = QuerydslR2dbcFragment.class;
    private final MySqlR2dbcQueryFactory queryFactory;
    private final R2dbcConverter converter;

    private final QuerydslR2dbcExpressionFactory querydslExpressionFactory = new QuerydslR2dbcExpressionFactory(REPOSITORY_TARGET_TYPE);

    public QuerydslR2dbcRepositoryFactory(@Nonnull final R2dbcEntityOperations operations,
                                          @Nonnull final MySqlR2dbcQueryFactory queryFactory) {
        super(operations);
        this.converter = operations.getConverter();
        this.queryFactory = queryFactory;
    }

    @Nonnull
    @Override
    protected RepositoryComposition.RepositoryFragments getRepositoryFragments(@Nonnull final RepositoryMetadata metadata) {
        final RepositoryComposition.RepositoryFragments fragments = super.getRepositoryFragments(metadata);
        final Class<?> repositoryInterface = metadata.getRepositoryInterface();
        if (!REPOSITORY_TARGET_TYPE.isAssignableFrom(repositoryInterface)) {
            return fragments;
        }
        final RelationalPath<?> path = querydslExpressionFactory.getRelationalPathFromQueryRepositoryClass(repositoryInterface);
        final Class<?> type = metadata.getDomainType();
        final ConstructorExpression<?> constructorExpression = querydslExpressionFactory.getConstructorExpression(type, path);
        //Fragment,Predicate
        final RepositoryFragment<Object> querydslR2dbcFragment = createQuerydslR2dbcFragment(path, constructorExpression);
        final RepositoryFragment<Object> querydslR2dbcPredicate = createQuerydslPredicateExecutor(path, constructorExpression);
        //
        return fragments.append(querydslR2dbcFragment).append(querydslR2dbcPredicate);
    }

    private RepositoryFragment<Object> createQuerydslR2dbcFragment(@Nonnull final RelationalPath<?> path,
                                                                   @Nonnull final ConstructorExpression<?> constructor) {
        final QuerydslR2dbcFragment<?> querydslR2dbcFragment = super.instantiateClass(QuerydslR2dbcFragmentDefault.class,
                constructor, path, queryFactory);
        return RepositoryFragment.implemented(querydslR2dbcFragment);
    }

    private RepositoryFragment<Object> createQuerydslPredicateExecutor(@Nonnull final RelationalPath<?> path,
                                                                       @Nonnull final ConstructorExpression<?> constructor) {
        final MappingContext<?, ?> context = this.converter.getMappingContext();
        final RelationalPersistentEntity<?> entity = (RelationalPersistentEntity<?>) context.getRequiredPersistentEntity(constructor.getType());
        final QuerydslR2dbc querydsl = new QuerydslR2dbc(entity);
        final QuerydslR2dbcPredicateExecutor<?> querydslR2dbcPredicate = super.instantiateClass(QuerydslR2dbcPredicateExecutorDefault.class,
                constructor, path, queryFactory, querydsl);
        return RepositoryFragment.implemented(querydslR2dbcPredicate);
    }
}
