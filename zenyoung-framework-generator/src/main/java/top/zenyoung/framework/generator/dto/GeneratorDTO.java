package top.zenyoung.framework.generator.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.framework.generator.config.GeneratorProperties;

import java.util.List;

/**
 * 代码生成配置请求DTO
 *
 * @author young
 */
@Data
@ApiModel("代码生成配置请求DTO")
@EqualsAndHashCode(callSuper = true)
public class GeneratorDTO extends GeneratorProperties {
    /**
     * 生成文件类型集合
     */
    @ApiModelProperty("生成文件类型集合")
    private List<String> includeFileTypes;
    /**
     * 模板分组
     */
    @ApiModelProperty("模板分组")
    private List<String> includeGroups;
}
