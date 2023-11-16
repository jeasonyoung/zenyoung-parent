package top.zenyoung.generator.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "代码文件信息")
@AllArgsConstructor(staticName = "of")
public class FileVO implements Serializable {
    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String dir;
    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String name;
    /**
     * 文件内容
     */
    @Schema(description = "文件内容")
    private String content;
}
