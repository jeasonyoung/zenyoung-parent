package top.zenyoung.generator.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "表信息VO")
public class TableVO implements Serializable {
    /**
     * 表名
     */
    @Schema(description = "表名")
    private String name;
    /**
     * 表注释
     */
    @Schema(description = "表注释")
    private String comment;
}
