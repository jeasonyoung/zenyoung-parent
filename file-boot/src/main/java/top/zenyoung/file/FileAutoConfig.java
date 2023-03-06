package top.zenyoung.file;

import com.google.common.base.Strings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.file.config.FileProperties;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.ServiceLoader;

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
    public FileService createFileService(@Nonnull final FileProperties properties) {
        final String typeName = properties.getType();
        if (!Strings.isNullOrEmpty(typeName)) {
            final ServiceLoader<FileServiceFactory> factories = ServiceLoader.load(FileServiceFactory.class);
            for (FileServiceFactory factory : factories) {
                if (Objects.nonNull(factory) && typeName.equalsIgnoreCase(factory.getType())) {
                    return factory.create(properties);
                }
            }
        }
        return null;
    }
}
