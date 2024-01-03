package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.relational.core.mapping.NamingStrategy;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.Objects;

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
    public com.querydsl.sql.Configuration querydslSqlConfiguration(@Nullable final SQLTemplates templates) {
        var sqlTemplates = Objects.isNull(templates) ? MySQLTemplates.DEFAULT : templates;
        return new com.querydsl.sql.Configuration(sqlTemplates);
    }

    @Bean
    @ConditionalOnMissingBean
    public SQLQueryFactory sqlQueryFactory(com.querydsl.sql.Configuration querydslSqlConfiguration) {
        return new SQLQueryFactory(querydslSqlConfiguration, (DataSource) null);
    }
}
