package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import top.zenyoung.framework.dto.BasePageDTO;

import java.util.Date;

/**
 * 登录日志-查询DTO
 *
 * @author young
 */
@Data
@ApiModel("登录日志-查询")
@EqualsAndHashCode(callSuper = true)
public class LoginLogQueryDTO extends BasePageDTO {
    /**
     * 用户账号
     */
    @ApiModelProperty("用户账号")
    private String account;
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
