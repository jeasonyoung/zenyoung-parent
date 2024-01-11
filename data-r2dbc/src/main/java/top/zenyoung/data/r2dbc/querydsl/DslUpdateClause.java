package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.core.types.Path;
import com.querydsl.sql.dml.SQLUpdateClause;
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
    private final SQLUpdateClause clause;

    public <T> DslUpdateClause set(final boolean condition, @Nonnull final Path<T> col, @Nonnull final Supplier<T> valHandler) {
        final T val = valHandler.get();
        return set(condition, col, val);
    }

    public <T> DslUpdateClause set(final boolean condition, @Nonnull final Path<T> col, @Nullable final T val) {
        if (condition) {
            if (!ref.get()) {
                ref.set(true);
            }
            clause.set(col, val);
        }
        return this;
    }

    public <T> DslUpdateClause set(@Nonnull final Path<T> col, @Nullable final T val) {
        return set(true, col, val);
    }

    public <T> DslUpdateClause set(@Nonnull final Path<T> col, @Nonnull final Supplier<T> valHandler) {
        return set(true, col, valHandler);
    }
}
