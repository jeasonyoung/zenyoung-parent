package top.zenyoung.file.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.file.FileProperties;

/**
 * 文件配置属性
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "top.zenyoung.file")
public class FileStorageProperties extends FileProperties {
    /**
     * 默认存储类型
     */
    private String type = "ali";
    /**
     * 默认文件桶
     */
    private String bucket;
}
