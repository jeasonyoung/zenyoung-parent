package top.zenyoung.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import top.zenyoung.generator.config.GeneratorAutoProperties;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * 代码生成器配置
 *
 * @author young
 */
@Slf4j
@Configuration
@ConditionalOnClass(DataSource.class)
@EnableConfigurationProperties(GeneratorAutoProperties.class)
@ComponentScan("top.zenyoung.generator.api")
public class AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Generator buildGenerator(final ObjectProvider<DataSource> dataSource) {
        log.info("buildGenerator init...");
        return new Generator(Objects.requireNonNull(dataSource.getIfAvailable()));
    }

    @Bean
    @ConditionalOnMissingBean(CorsFilter.class)
    @ConditionalOnProperty(prefix = "zenyoung.generator", name = "cors", havingValue = "true", matchIfMissing = true)
    public CorsFilter corsFilter() {
        log.info("init CorsFilter...");
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(10000L);
        //匹配所有API
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
}
