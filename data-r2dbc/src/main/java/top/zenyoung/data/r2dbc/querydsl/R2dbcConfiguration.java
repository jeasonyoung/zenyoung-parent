package top.zenyoung.data.r2dbc.querydsl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.querydsl.core.JoinExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.sql.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * R2dbc配置
 *
 * @author young
 */
@Configuration
@Import(R2dbcSQLTemplatesConfiguration.class)
public class R2dbcConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public NamingStrategy camelCaseNamingStrategy() {
        return new CamelCaseNamingStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public com.querydsl.sql.Configuration sqlConfiguration(@Nullable final SQLTemplates templates) {
        var sqlTemplates = Objects.isNull(templates) ? MySQLTemplates.DEFAULT : templates;
        return new com.querydsl.sql.Configuration(sqlTemplates);
    }

    @Bean
    @ConditionalOnMissingBean
    public SQLQueryFactory sqlQueryFactory(@Nonnull final com.querydsl.sql.Configuration sqlConfiguration) {
        return new SQLQueryFactoryInner(sqlConfiguration);
    }

    private static class SQLQueryFactoryInner extends SQLQueryFactory {
        public SQLQueryFactoryInner(@Nonnull final com.querydsl.sql.Configuration sqlConfiguration) {
            super(sqlConfiguration, (DataSource) null);
        }

        @Override
        public SQLQuery<?> query() {
            return new SQLQueryInner<>(connection, configuration);
        }
    }

    private static class SQLQueryInner<T> extends SQLQuery<T> {
        public SQLQueryInner(final Supplier<Connection> connProvider, final com.querydsl.sql.Configuration configuration) {
            super(connProvider, configuration);
        }

        @Override
        public SQLQuery<T> from(final Expression<?> arg) {
            if (arg instanceof EntityPath<?> entityPath) {
                final RelationalPath<?> rp = QuerydslExpressionFactory.fromEntityPath(entityPath);
                return super.from(rp);
            }
            return super.from(arg);
        }

        @Override
        public SQLQuery<T> from(final Expression<?>... args) {
            if (Objects.nonNull(args) && args.length > 0) {
                final Expression<?>[] exps = Stream.of(args)
                        .map(arg -> {
                            if (arg instanceof EntityPath<?> entityPath) {
                                return QuerydslExpressionFactory.fromEntityPath(entityPath);
                            }
                            return arg;
                        })
                        .toArray(Expression[]::new);
                return super.from(exps);
            }
            return super.from(args);
        }
        
        @Nonnull
        @Override
        protected SQLSerializer createSerializer() {
            final SQLSerializer serializer = new SQLSerializerInner(configuration);
            serializer.setUseLiterals(useLiterals);
            return serializer;
        }
    }

    private static class SQLSerializerInner extends SQLSerializer {

        public SQLSerializerInner(final com.querydsl.sql.Configuration conf) {
            super(conf);
        }

        @Override
        protected void appendAsColumnName(final Path<?> path, final boolean precededByDot) {
            String column = ColumnMetadata.getName(path);
            if (!Strings.isNullOrEmpty(column)) {
                final var parent = path.getMetadata().getParent();
                if (parent instanceof EntityPathBase<?> entityPath) {
                    final String colName;
                    if (!Strings.isNullOrEmpty(colName = QuerydslExpressionFactory.getTableColumn(entityPath, column))) {
                        column = colName;
                    }
                } else if (parent instanceof RelationalPathBase<?> relationalPath) {
                    final String colName;
                    if (Strings.isNullOrEmpty(colName = QuerydslExpressionFactory.getTableColumn(relationalPath, column))) {
                        column = colName;
                    }
                }
            }
            append(templates.quoteIdentifier(column, precededByDot));
        }

        @Override
        protected List<Expression<?>> getIdentifierColumns(@Nonnull final List<JoinExpression> joins, final boolean alias) {
            final BiFunction<Expression<?>, Function<RelationalPath<?>, List<Path<?>>>, List<Expression<?>>> targetToExpressionsHandler = (target, handler) -> {
                List<Path<?>> columns = null;
                if (target instanceof EntityPath<?> entityPath) {
                    final var relationalPath = QuerydslExpressionFactory.fromEntityPath(entityPath);
                    if (Objects.nonNull(relationalPath)) {
                        columns = handler.apply(relationalPath);
                    }
                }
                if (target instanceof RelationalPath<?> rp) {
                    columns = handler.apply(rp);
                }
                if (!CollectionUtils.isEmpty(columns)) {
                    return columns.stream()
                            .map(col -> (Expression<?>) col)
                            .collect(Collectors.toList());
                }
                return Lists.newArrayList();
            };
            if (joins.size() == 1) {
                final JoinExpression join = joins.get(0);
                return targetToExpressionsHandler.apply(join.getTarget(), RelationalPath::getColumns);
            }
            final List<Expression<?>> rv = Lists.newArrayList();
            for (final JoinExpression join : joins) {
                final List<Expression<?>> columns = targetToExpressionsHandler.apply(join.getTarget(), rp -> {
                    final PrimaryKey<?> pk;
                    if (Objects.nonNull(pk = rp.getPrimaryKey())) {
                        return pk.getLocalColumns().stream()
                                .map(lc -> (Path<?>) lc)
                                .collect(Collectors.toList());
                    }
                    return rp.getColumns();
                });
                if (!CollectionUtils.isEmpty(columns)) {
                    if (alias) {
                        final AtomicInteger refIdx = new AtomicInteger(0);
                        columns.forEach(column -> rv.add(ExpressionUtils.as(column, "col" + refIdx.incrementAndGet())));
                    } else {
                        rv.addAll(columns);
                    }
                }
            }
            return rv;
        }

        @Override
        protected void handleJoinTarget(final JoinExpression je) {
            if (je.getTarget() instanceof EntityPathBase<?> entityPath) {
                final var pe = QuerydslExpressionFactory.fromEntityPath(entityPath);
                final var nJe = new JoinExpression(je.getType(), pe, je.getCondition(), je.getFlags());
                super.handleJoinTarget(nJe);
            } else {
                super.handleJoinTarget(je);
            }
        }

    }
}
