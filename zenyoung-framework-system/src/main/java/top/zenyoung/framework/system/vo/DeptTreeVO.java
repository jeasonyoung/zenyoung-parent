package top.zenyoung.framework.system.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.framework.system.dto.DeptLoadDTO;

import java.util.List;

/**
 * 部门树响应数据
 *
 * @author young
 */
@Data
@ApiModel("部门-部门树-响应报文")
@EqualsAndHashCode(callSuper = true)
public class DeptTreeVO extends DeptLoadDTO {
    /**
     * 子部门集合
     */
    private List<DeptTreeVO> children;
}
