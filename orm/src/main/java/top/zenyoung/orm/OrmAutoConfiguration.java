package top.zenyoung.orm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.orm.config.MapperScannerRegister;
import top.zenyoung.orm.config.MybatisPlusConfig;

/**
 * Orm 自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@Import({MybatisPlusConfig.class, MapperScannerRegister.class})
public class OrmAutoConfiguration {

}
