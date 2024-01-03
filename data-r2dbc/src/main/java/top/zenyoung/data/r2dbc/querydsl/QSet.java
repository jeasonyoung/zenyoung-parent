package top.zenyoung.data.r2dbc.querydsl;

import com.google.common.collect.Sets;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * QSet
 *
 * @author young
 */
public class QSet extends FactoryExpressionBase<Set<?>> {
    private final List<Expression<?>> args;

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected QSet(@Nonnull final Expression<?>... args) {
        super((Class) Set.class);
        this.args = CollectionUtils.unmodifiableList(Arrays.asList(args));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected QSet(@Nonnull final List<Expression<?>> args) {
        super((Class) Set.class);
        this.args = CollectionUtils.unmodifiableList(args);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected QSet(@Nonnull final Expression<?>[]... args) {
        super((Class) Set.class);
        final List<Expression<?>> builder = new ArrayList<>();
        for (var exprs : args) {
            Collections.addAll(builder, exprs);
        }
        this.args = CollectionUtils.unmodifiableList(builder);
    }

    @Override
    @Nullable
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(this, context);
    }

    @Override
    public List<Expression<?>> getArgs() {
        return args;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof FactoryExpression<?> fe) {
            return args.equals(fe.getArgs()) && getType().equals(fe.getType());
        } else {
            return false;
        }
    }

    @Override
    @Nullable
    public Set<?> newInstance(@Nonnull final Object... args) {
        return Collections.unmodifiableSet(Sets.newLinkedHashSet(Arrays.asList(args)));
    }
}
