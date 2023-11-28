package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.r2dbc.dml.R2dbcUpdateClause;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQueryFactory;
import com.querydsl.sql.RelationalPath;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor
public class QuerydslR2dbcFragmentDefault<T> implements QuerydslR2dbcFragment<T> {
    private final ConstructorExpression<T> constructorExpression;
    private final RelationalPath<T> path;
    private final MySqlR2dbcQueryFactory queryFactory;

    @Override
    public Mono<Long> update(@Nonnull final UnaryOperator<R2dbcUpdateClause> update) {
        R2dbcUpdateClause updateClause = queryFactory.update(path);
        updateClause = update.apply(updateClause);
        return updateClause.execute();
    }

    @Override
    public Mono<Long> deleteWhere(@Nonnull final Predicate predicate) {
        return queryFactory.delete(path)
                .where(predicate)
                .execute();
    }

    @Override
    public Expression<T> entityProjection() {
        return constructorExpression;
    }
}
