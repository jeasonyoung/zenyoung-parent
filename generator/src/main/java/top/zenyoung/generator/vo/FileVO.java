package top.zenyoung.generator.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * 代码文件信息
 *
 * @author young
 */
@Data
@ApiModel("代码文件信息")
@RequiredArgsConstructor(staticName = "of")
public class FileVO implements Serializable {
    /**
     * 文件路径
     */
    @ApiModelProperty("文件路径")
    private final String dir;
    /**
     * 文件名
     */
    @ApiModelProperty("文件名")
    private final String name;
    /**
     * 文件内容
     */
    @ApiModelProperty("文件内容")
    private final String content;
}
