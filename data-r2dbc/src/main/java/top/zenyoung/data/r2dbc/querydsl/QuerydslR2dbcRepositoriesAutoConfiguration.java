package top.zenyoung.data.r2dbc.querydsl;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({R2dbcConfiguration.class, QuerydslR2dbcRepositoriesAutoConfigureRegistrar.class})
@AutoConfigureBefore(R2dbcRepositoriesAutoConfiguration.class)
public class QuerydslR2dbcRepositoriesAutoConfiguration {
    
}
