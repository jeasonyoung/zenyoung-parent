package top.zenyoung.framework.system.dao.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门-加载-数据DTO
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptLoadDTO extends DeptModifyDTO {
    /**
     * 祖级列表
     */
    private String ancestors;
}
