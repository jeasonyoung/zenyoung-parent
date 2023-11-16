package top.zenyoung.generator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.generator.config.GeneratorProperties;

import java.util.List;

/**
 * 代码生成配置请求DTO
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "代码生成配置请求DTO")
public class GeneratorDTO extends GeneratorProperties {
    /**
     * 模板分组
     */
    @Schema(description = "模板分组")
    private String includeGroup;
    /**
     * 生成文件类型集合
     */
    @Schema(description = "生成文件类型集合")
    private List<String> includeFileTypes;
}
