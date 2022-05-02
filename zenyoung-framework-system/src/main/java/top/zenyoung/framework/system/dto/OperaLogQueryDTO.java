package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import top.zenyoung.framework.dto.BasePageDTO;

import java.util.Date;

/**
 * 操作记录-新增DTO
 *
 * @author young
 */
@Data
@ApiModel("操作记录-新增")
@EqualsAndHashCode(callSuper = true)
public class OperaLogQueryDTO extends BasePageDTO {
    /**
     * 模块标题/方法名称
     */
    @ApiModelProperty("模块标题/方法名称")
    private String title;
    /**
     * 开始日期(yyyy-MM-dd)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("开始日期(yyyy-MM-dd)")
    private Date start;
    /**
     * 结束日期(yyyy-MM-dd)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("开始日期(yyyy-MM-dd)")
    private Date end;
}
