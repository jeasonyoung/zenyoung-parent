package top.zenyoung.framework.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 操作日志删除-DTO
 *
 * @author young
 */
@Data
@ApiModel("操作日志删除")
public class OperaLogDelDTO {
    /**
     * 方法名称
     */
    @ApiModelProperty("方法名称")
    private String method;
    /**
     * 开始日期(yyyy-MM-dd)
     */
    @ApiModelProperty("开始日期(yyyy-MM-dd)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date start;
    /**
     * 结束日期(yyyy-MM-dd)
     */
    @ApiModelProperty("结束日期(yyyy-MM-dd)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date end;
}
