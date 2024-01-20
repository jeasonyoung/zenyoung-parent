package top.zenyoung.data.r2dbc;

import com.google.common.collect.Lists;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.r2dbc.core.DatabaseClient;
import top.zenyoung.data.r2dbc.converter.EnumReadConverter;
import top.zenyoung.data.r2dbc.converter.EnumWriterConverter;
import top.zenyoung.data.r2dbc.converter.ZonedDateTimeToDateConverter;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * R2dbc自定义类型转换-自动注入
 *
 * @author young
 */
@Configuration
@AutoConfiguration(before = R2dbcDataAutoConfiguration.class)
public class R2dbcConversionsAutoConfiguration {
    @Bean
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
        return Lists.newArrayList(new EnumReadConverter(), new EnumWriterConverter(), new ZonedDateTimeToDateConverter());
    }
}
