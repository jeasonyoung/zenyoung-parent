package top.zenyoung.generator.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 代码文件信息
 *
 * @author young
 */
@Data
@NoArgsConstructor
@ApiModel("代码文件信息")
@AllArgsConstructor(staticName = "of")
public class FileVO implements Serializable {
    /**
     * 文件路径
     */
    @ApiModelProperty("文件路径")
    private String dir;
    /**
     * 文件名
     */
    @ApiModelProperty("文件名")
    private String name;
    /**
     * 文件内容
     */
    @ApiModelProperty("文件内容")
    private String content;
}
