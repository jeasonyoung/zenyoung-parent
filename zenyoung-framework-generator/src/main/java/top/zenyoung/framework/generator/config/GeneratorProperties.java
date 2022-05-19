package top.zenyoung.framework.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 代码生成器配置
 *
 * @author young
 */
@Data
@ConfigurationProperties(prefix = "top.zenyoung.generator")
public class GeneratorProperties {
    /**
     * 是否启用
     */
    private boolean enable = true;
    /**
     * 作者
     */
    private String author = "zenyoung";
    /**
     * 默认生成包路径
     */
    private String packageName = "top.zenyoung";
    /**
     * 自动去除表前缀,默认是false
     */
    private Boolean autoRemovePrefix = true;
    /**
     * 表前缀(生成类名不会包含表前缀,多个用逗号分隔)
     */
    private String tablePrefix = "tbl_";
    /**
     * 页面忽略表字段名称(多个用逗号分隔)
     */
    private String pageIgnoreColumns = "create_date,create_by,update_date,update_by";
}
