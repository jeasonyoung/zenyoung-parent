package top.zenyoung.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 代码生成器配置
 *
 * @author young
 */
@Data
@ConfigurationProperties(prefix = "gen")
public class GenConfig {
    /**
     * 作者
     */
    private String author;
    /**
     * 默认生成包路径
     */
    private String packageName;
    /**
     * 自动去除表前缀,默认是false
     */
    private Boolean autoRemovePrefix;
    /**
     * 表前缀(生成类名不会包含表前缀,多个用逗号分隔)
     */
    private String tablePrefix;
    /**
     * 页面忽略表字段名称(多个用逗号分隔)
     */
    private String pageIgnoreColumns;
}
