package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置-新增DTO
 *
 * @author young
 */
@Data
@ApiModel("参数配置-新增")
@EqualsAndHashCode(callSuper = true)
public class ConfigAddDTO extends ConfigBaseDTO {

}