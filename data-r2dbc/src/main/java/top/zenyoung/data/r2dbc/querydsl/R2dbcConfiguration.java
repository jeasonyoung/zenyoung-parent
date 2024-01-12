package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.sql.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.relational.core.mapping.NamingStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Objects;
import java.util.function.Supplier;

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
