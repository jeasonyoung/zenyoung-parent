package top.zenyoung.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.storage.config.StorageProperties;

/**
 * 存储-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({StorageProperties.class})
public class StorageAutoConfiguration {
    
}
