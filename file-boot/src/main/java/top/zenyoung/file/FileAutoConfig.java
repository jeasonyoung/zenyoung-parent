package top.zenyoung.file;

import com.google.common.base.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.common.util.SpiUtils;
import top.zenyoung.file.config.FileProperties;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * 文件-自动注入
 *
 * @author young
 */
@Configuration
@ComponentScan({"top.zenyoung.file.service"})
@EnableConfigurationProperties({FileProperties.class})
public class FileAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public FileService createFileService(@Nonnull final FileProperties properties) {
        final String typeName = properties.getType();
        if (!Strings.isNullOrEmpty(typeName)) {
            final FileServiceFactory factory = SpiUtils.load(FileServiceFactory.class, srv -> typeName.equalsIgnoreCase(srv.getType()));
            if (Objects.nonNull(factory)) {
                return factory.create(properties);
            }
        }
        return null;
    }
}
