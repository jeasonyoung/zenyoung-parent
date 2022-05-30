package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 在线用户-数据DTO
 *
 * @author young
 */
@Data
@ApiModel("在线用户")
public class OnlineDTO implements Serializable {
    /**
     * 在线用户Key
     */
    @ApiModelProperty("在线用户Key")
    private String key;
    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    private String id;
    /**
     * 用户账号
     */
    @ApiModelProperty("用户账号")
    private String account;
    /**
     * 用户角色集合
     */
    @ApiModelProperty("用户角色集合")
    private List<String> roles;
    /**
     * 设备标识
     */
    @ApiModelProperty("设备标识")
    private String device;
}
