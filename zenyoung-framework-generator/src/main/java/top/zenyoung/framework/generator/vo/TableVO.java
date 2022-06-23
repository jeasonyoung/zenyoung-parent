package top.zenyoung.framework.generator.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 表信息
 *
 * @author young
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("表信息VO")
public class TableVO implements Serializable {
    /**
     * 表名
     */
    @ApiModelProperty("表名")
    private String name;
    /**
     * 表注释
     */
    @ApiModelProperty("表注释")
    private String comment;
}
