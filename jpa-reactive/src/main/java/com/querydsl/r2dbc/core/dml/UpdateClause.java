package com.querydsl.r2dbc.core.dml;

import com.querydsl.core.types.Path;

import javax.annotation.Nonnull;
import java.util.List;

public interface UpdateClause<C extends UpdateClause<C>> extends StoreClause<C>, FilteredClause<C> {

    C set(@Nonnull final List<? extends Path<?>> paths, @Nonnull final List<?> vals);
}
