package com.querydsl.r2dbc.core.dml;

import com.querydsl.core.types.Predicate;

public interface FilteredClause<C extends FilteredClause<C>> {
    C where(final Predicate... o);
}
