package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLUpdateClause;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * R2dbc Fragment 查询接口
 *
 * @param <T> 实体类型
 * @author young
 */
public interface QuerydslR2dbcFragment<T> {
    /**
     * 查询构建
     *
     * @param builder 构建处理器
     * @param <O>     查询结果类型
     * @return 构建结果
     */
    <O> RowsFetchSpec<O> query(@Nonnull final Function<SQLQuery<?>, SQLQuery<O>> builder);

    /**
     * 更新处理
     *
     * @param update 更新处理器
     * @return 更新结果
     */
    Mono<Long> update(@Nonnull final UnaryOperator<SQLUpdateClause> update);

    /**
     * 条件删除
     *
     * @param predicate 删除条件
     * @return 删除结果
     */
    Mono<Long> deleteWhere(@Nonnull final Predicate predicate);

    /**
     * Returns entity projection used for mapping {@code QT} to {@code T}.
     *
     * @return entity projection
     */
    Expression<T> entityProjection();
}
