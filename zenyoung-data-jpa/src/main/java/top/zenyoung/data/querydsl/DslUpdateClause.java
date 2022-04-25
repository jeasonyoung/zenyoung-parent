package top.zenyoung.data.querydsl;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DSL更新处理
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class DslUpdateClause {
    private final AtomicInteger ref = new AtomicInteger(0);
    private final JPAUpdateClause clause;

    /**
     * 添加更新列数据
     *
     * @param condition 是否更新
     * @param col       更新列
     * @param data      更新数据
     * @param <T>       更新数据类型
     * @param <K>       更新列数据类型
     * @return 链式对象
     */
    public <T, K extends Path<T>> DslUpdateClause add(final boolean condition, final K col, final T data) {
        if (condition && col != null && data != null) {
            clause.set(col, data);
            ref.getAndIncrement();
        }
        return this;
    }

    /**
     * 添加更新列数据
     *
     * @param col  更新列
     * @param data 更新数据
     * @param <T>  更新数据类型
     * @param <K>  更新列数据类型
     * @return 链式对象
     */
    public <T, K extends Path<T>> DslUpdateClause add(final K col, final T data) {
        return add(true, col, data);
    }

    /**
     * 执行更新处理
     *
     * @param where 更新条件
     * @return 更新结果
     */
    public boolean execute(@Nonnull final Predicate... where) {
        if (ref.get() > 0 && where.length > 0) {
            return clause.where(where).execute() > 0;
        }
        return false;
    }

}
