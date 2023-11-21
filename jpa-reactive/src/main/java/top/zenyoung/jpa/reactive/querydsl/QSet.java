package top.zenyoung.jpa.reactive.querydsl;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class QSet extends FactoryExpressionBase<Set<?>> {
    private final List<Expression<?>> args;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public QSet(@Nonnull final Expression<?>... args) {
        super((Class) Set.class);
        this.args = CollectionUtils.unmodifiableList(Lists.newArrayList(args));
    }

    @Override
    public List<Expression<?>> getArgs() {
        return this.args;
    }

    @Override
    public Set<?> newInstance(@Nonnull final Object... args) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(args)));
    }

    @Override
    public <R, C> R accept(@Nonnull final Visitor<R, C> v, @Nullable final C context) {
        return v.visit(this, context);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof FactoryExpression) {
            final FactoryExpression<?> c = (FactoryExpression<?>) o;
            return args.equals(c.getArgs()) && getType().equals(c.getType());
        }
        return false;
    }
}
