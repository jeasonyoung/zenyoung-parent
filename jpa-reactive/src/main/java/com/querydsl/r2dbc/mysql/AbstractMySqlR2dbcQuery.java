package com.querydsl.r2dbc.mysql;

import com.google.common.base.Joiner;
import com.querydsl.core.JoinFlag;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.r2dbc.AbstractR2dbcQuery;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.sql.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public abstract class AbstractMySqlR2dbcQuery<T, C extends AbstractMySqlR2dbcQuery<T, C>> extends AbstractR2dbcQuery<T, C> {
    protected static final String WITH_ROLLUP = "\nwith rollup ";
    protected static final String STRAIGHT_JOIN = "straight_join ";
    protected static final String SQL_SMALL_RESULT = "sql_small_result ";
    protected static final String SQL_NO_CACHE = "sql_no_cache ";
    protected static final String LOCK_IN_SHARE_MODE = "\nlock in share mode ";
    protected static final String HIGH_PRIORITY = "high_priority ";
    protected static final String SQL_CALC_FOUND_ROWS = "sql_calc_found_rows ";
    protected static final String SQL_CACHE = "sql_cache ";
    protected static final String SQL_BUFFER_RESULT = "sql_buffer_result ";
    protected static final String SQL_BIG_RESULT = "sql_big_result ";
    protected static final Joiner JOINER = Joiner.on(", ");

    public AbstractMySqlR2dbcQuery(@Nonnull final R2dbcConnectionProvider provider,
                                   @Nonnull final Configuration configuration,
                                   @Nonnull final QueryMetadata metadata) {
        super(provider, configuration, metadata);
    }

    public C bigResult() {
        return addFlag(QueryFlag.Position.AFTER_SELECT, SQL_BIG_RESULT);
    }

    public C bufferResult() {
        return addFlag(QueryFlag.Position.AFTER_SELECT, SQL_BUFFER_RESULT);
    }

    public C cache() {
        return addFlag(QueryFlag.Position.AFTER_SELECT, SQL_CACHE);
    }

    public C calcFoundRows() {
        return addFlag(QueryFlag.Position.AFTER_SELECT, SQL_CALC_FOUND_ROWS);
    }

    public C highPriority() {
        return addFlag(QueryFlag.Position.AFTER_SELECT, HIGH_PRIORITY);
    }

    public C into(@Nonnull final String var) {
        return addFlag(QueryFlag.Position.END, "\ninto " + var);
    }

    public C intoDumpfile(@Nonnull final File file) {
        return addFlag(QueryFlag.Position.END, "\ninto dumpfile '" + file.getPath() + "'");
    }

    public C intoOutfile(@Nonnull final File file) {
        return addFlag(QueryFlag.Position.END, "\ninto outfile '" + file.getPath() + "'");
    }

    public C lockInShareMode() {
        return addFlag(QueryFlag.Position.END, LOCK_IN_SHARE_MODE);
    }

    public C noCache() {
        return addFlag(QueryFlag.Position.AFTER_SELECT, SQL_NO_CACHE);
    }

    public C smallResult() {
        return addFlag(QueryFlag.Position.AFTER_SELECT, SQL_SMALL_RESULT);
    }

    public C straightJoin() {
        return addFlag(QueryFlag.Position.AFTER_SELECT, STRAIGHT_JOIN);
    }

    public C forceIndex(@Nonnull final String... indexes) {
        return addJoinFlag(" force index (" + JOINER.join(indexes) + ")", JoinFlag.Position.END);
    }

    public C ignoreIndex(@Nonnull final String... indexes) {
        return addJoinFlag(" ignore index (" + JOINER.join(indexes) + ")", JoinFlag.Position.END);
    }

    public C useIndex(@Nonnull final String... indexes) {
        return addJoinFlag(" use index (" + JOINER.join(indexes) + ")", JoinFlag.Position.END);
    }

    public C withRollup() {
        return addFlag(QueryFlag.Position.AFTER_GROUP_BY, WITH_ROLLUP);
    }
}
