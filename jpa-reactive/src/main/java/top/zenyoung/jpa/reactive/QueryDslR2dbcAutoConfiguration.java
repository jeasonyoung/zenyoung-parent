package top.zenyoung.jpa.reactive;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * R2dbc querydsl 自动配置
 *
 * @author young
 */
@Configuration
@Import({QueryDslR2dbcAutoConfigurationRegistrar.class})
@AutoConfigureBefore(R2dbcRepositoriesAutoConfiguration.class)
public class QueryDslR2dbcAutoConfiguration {

}
