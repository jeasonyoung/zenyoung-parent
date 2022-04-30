package top.zenyoung.framework.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.framework.system.dto.MenuDTO;

import java.util.List;

/**
 * 菜单树
 *
 * @author young
 */
@Data
@ApiModel("菜单树")
@EqualsAndHashCode(callSuper = true)
public class MenuTreeVO extends MenuDTO {
    /**
     * 子菜单集合
     */
    @ApiModelProperty("子菜单集合")
    private List<MenuTreeVO> children;
}
