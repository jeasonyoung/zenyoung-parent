package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SQLTemplatesRegistry;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.sql.SQLException;

@Configuration
@ConditionalOnClass(Flyway.class)
public class R2dbcSQLTemplatesConfiguration {

    @Bean
    @ConditionalOnBean(Flyway.class)
    public SQLTemplates sqlTemplates(@Nonnull final Flyway flyway) throws SQLException {
        try (var connectionFactory = new JdbcConnectionFactory(
                flyway.getConfiguration().getDataSource(), flyway.getConfiguration(), null)) {
            var sqlTemplatesRegistry = new SQLTemplatesRegistry();
            try (var conn = connectionFactory.openConnection()) {
                return sqlTemplatesRegistry.getTemplates(conn.getMetaData());
            }
        }
    }
}
