package top.zenyoung.orm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.boot.util.IdSequenceUtils;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.orm.config.MapperScannerRegister;
import top.zenyoung.orm.config.MybatisPlusConfig;
import top.zenyoung.orm.config.OrmIdSequenceProperties;

/**
 * Orm 自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({OrmIdSequenceProperties.class})
@Import({MybatisPlusConfig.class, MapperScannerRegister.class})
public class OrmAutoConfiguration {

    @Bean("ormIdSequence")
    @ConditionalOnMissingBean
    public IdSequence buildSequence(final ObjectProvider<OrmIdSequenceProperties> provider) {
        return IdSequenceUtils.create(provider.getIfAvailable());
    }
}
