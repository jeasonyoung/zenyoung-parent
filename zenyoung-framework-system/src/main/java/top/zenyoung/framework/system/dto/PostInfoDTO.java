package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 岗位-信息DTO
 *
 * @author young
 */
@Data
@AllArgsConstructor(staticName = "of")
public class PostInfoDTO {
    /**
     * 岗位ID
     */
    @ApiModelProperty("岗位ID")
    private Long id;
    /**
     * 岗位名称
     */
    @ApiModelProperty("岗位名称")
    private String name;
}
