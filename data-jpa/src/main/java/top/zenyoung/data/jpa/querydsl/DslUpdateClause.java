package top.zenyoung.data.jpa.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * DSL更新处理
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class DslUpdateClause {
    private final AtomicBoolean ref = new AtomicBoolean(false);
    private final JPAUpdateClause clause;

    public <T> DslUpdateClause add(final boolean condition, @Nonnull final Path<T> col, @Nonnull final Supplier<T> valHandler) {
        final T val = valHandler.get();
        return add(condition, col, val);
    }

    public <T> DslUpdateClause add(final boolean condition, @Nonnull final Path<T> col, @Nullable final T val) {
        if (condition) {
            if (!ref.get()) {
                ref.set(true);
            }
            clause.set(col, val);
        }
        return this;
    }

    public <T> DslUpdateClause add(final boolean condition, @Nonnull final Path<T> col, @Nullable final Expression<? extends T> expression) {
        if (condition) {
            if (!ref.get()) {
                ref.set(true);
            }
            clause.set(col, expression);
        }
        return this;
    }

    public <T> DslUpdateClause add(@Nonnull final Path<T> col, @Nullable final T val) {
        return add(true, col, val);
    }

    public <T> DslUpdateClause add(@Nonnull final Path<T> col, @Nullable final Expression<? extends T> expression) {
        return add(true, col, expression);
    }

    public <T> DslUpdateClause add(@Nonnull final Path<T> col, @Nonnull final Supplier<T> valHandler) {
        return add(true, col, valHandler);
    }

    public boolean execute(@Nonnull final Predicate where) {
        if (ref.get()) {
            return clause.where(where).execute() > 0;
        }
        return false;
    }
}
