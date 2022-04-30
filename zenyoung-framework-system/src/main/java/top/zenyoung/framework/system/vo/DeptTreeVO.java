package top.zenyoung.framework.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.framework.system.dto.DeptDTO;

import java.util.List;

/**
 * 部门树
 *
 * @author young
 */
@Data
@ApiModel("部门树")
@EqualsAndHashCode(callSuper = true)
public class DeptTreeVO extends DeptDTO {
    /**
     * 子部门集合
     */
    @ApiModelProperty("子部门集合")
    private List<DeptTreeVO> children;
}
