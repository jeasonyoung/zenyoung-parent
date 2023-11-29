package com.querydsl.r2dbc;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface R2dbcMapper<T> {
    @Nonnull
    T map(@Nonnull final Row row, @Nonnull final RowMetadata metadata);
}
