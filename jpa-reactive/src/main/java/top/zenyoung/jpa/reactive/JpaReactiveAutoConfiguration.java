package top.zenyoung.jpa.reactive;

import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQueryFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.r2dbc.core.DatabaseClient;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.IdSequenceProperties;
import top.zenyoung.common.sequence.SnowFlake;

import javax.annotation.Nonnull;

/**
 * Jpa-反应式自动配置
 *
 * @author young
 */
@Configuration
@EnableR2dbcAuditing
public class JpaReactiveAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Bean
    @ConditionalOnMissingBean
    public IdSequence idSequence() {
        final IdSequenceProperties properties = context.getBean(IdSequenceProperties.class);
        return SnowFlake.create(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveAuditorAware<String> reactiveAuditorAware() {
        return () -> SecurityUtils.getContext()
                .map(UserPrincipal::getAccount);
    }

    @Bean
    @ConditionalOnMissingBean
    public R2dbcConnectionProvider connectionProvider(@Nonnull final DatabaseClient client) {
        return () -> ConnectionFactoryUtils.getConnection(client.getConnectionFactory());
    }

    @Bean
    @ConditionalOnMissingBean
    public MySqlR2dbcQueryFactory queryFactory(@Nonnull final R2dbcConnectionProvider provider) {
        return new MySqlR2dbcQueryFactory(provider);
    }
}
