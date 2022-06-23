package top.zenyoung.framework.generator.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.framework.generator.config.GeneratorProperties;

/**
 * 代码生成配置请求DTO
 *
 * @author young
 */
@Data
@ApiModel("代码生成配置请求DTO")
@EqualsAndHashCode(callSuper = true)
public class GeneratorDTO extends GeneratorProperties {

}
