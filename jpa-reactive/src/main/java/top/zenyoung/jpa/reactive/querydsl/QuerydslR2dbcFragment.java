package top.zenyoung.jpa.reactive.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.r2dbc.dml.R2dbcUpdateClause;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.function.UnaryOperator;

@NoRepositoryBean
public interface QuerydslR2dbcFragment<T> {

    /**
     * 更新处理
     *
     * @param update 更新处理器
     * @return 处理结果
     */
    Mono<Long> update(@Nonnull final UnaryOperator<R2dbcUpdateClause> update);

    /**
     * Deletes all entities matching the given {@link Predicate}.
     *
     * @param predicate to match
     * @return amount of affected rows
     */
    Mono<Long> deleteWhere(@Nonnull final Predicate predicate);

    /**
     * Returns entity projection used for mapping {@code QT} to {@code T}.
     *
     * @return entity projection
     */
    Expression<T> entityProjection();
}
