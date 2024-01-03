package top.zenyoung.data.r2dbc;

import com.google.common.collect.Lists;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.r2dbc.core.DatabaseClient;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.data.r2dbc.converter.EnumReadConverter;
import top.zenyoung.data.r2dbc.converter.EnumWriterConverter;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * R2dbc-反应式自动配置
 *
 * @author young
 */
@Configuration
@EnableR2dbcAuditing
public class R2dbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReactiveAuditorAware<String> reactiveAuditorAware() {
        return () -> SecurityUtils.getPrincipal()
                .map(UserPrincipal::getAccount);
    }

    @Bean
    @ConditionalOnMissingBean
    public R2dbcCustomConversions r2dbcCustomConversions(@Nonnull final DatabaseClient client) {
        final R2dbcDialect dialect = DialectResolver.getDialect(client.getConnectionFactory());
        final List<Object> converters = Lists.newArrayList(dialect.getConverters());
        converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS);
        return new R2dbcCustomConversions(
                CustomConversions.StoreConversions.of(dialect.getSimpleTypeHolder(), converters),
                getCustomConverters()
        );
    }

    protected List<Object> getCustomConverters() {
        return Lists.newArrayList(new EnumReadConverter(), new EnumWriterConverter());
    }
}
