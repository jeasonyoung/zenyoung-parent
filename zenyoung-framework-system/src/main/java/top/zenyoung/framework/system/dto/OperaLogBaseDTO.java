package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;
import top.zenyoung.framework.common.OperaBizType;

import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * 操作记录-基础DTO
 */
@Data
class OperaLogBaseDTO implements Serializable {
    /**
     * 模块标题
     */
    @ApiModelProperty("模块标题")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "模块标题长度不超过128个字符")
    private String title;
    /**
     * 业务类型(0:查询,1:新增,2:修改,3:删除,4:其它)
     */
    @ApiModelProperty("业务类型")
    private OperaBizType type;
    /**
     * 方法名称
     */
    @ApiModelProperty("方法名称")
    private String method;
    /**
     * 请求URL
     */
    @ApiModelProperty("请求URL")
    @Max(groups = {Insert.class, Modify.class}, value = 512, message = "请求URL长度不超过512个字符")
    private String url;
    /**
     * 请求方式
     */
    @ApiModelProperty("请求方式")
    @Max(groups = {Insert.class, Modify.class}, value = 32, message = "请求方式长度不超过32个字符")
    private String reqMethod;
    /**
     * 操作人员
     */
    @ApiModelProperty("操作人员")
    @Max(groups = {Insert.class, Modify.class}, value = 64, message = "操作人员长度不超过64个字符")
    private String operaName;
    /**
     * 操作IP地址
     */
    @ApiModelProperty("操作IP地址")
    @Max(groups = {Insert.class, Modify.class}, value = 32, message = "操作IP地址长度不超过32个字符")
    private String operaIp;
    /**
     * 操作地址
     */
    @ApiModelProperty("操作地址")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "操作地址长度不超过128个字符")
    private String operaLocation;
    /**
     * 操作设备
     */
    @ApiModelProperty("操作设备")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "操作设备长度不超过128个字符")
    private String operaDevice;
    /**
     * 请求参数
     */
    @ApiModelProperty("请求参数")
    private String operaParam;
    /**
     * 返回参数
     */
    @ApiModelProperty("返回参数")
    private String operaResult;
    /**
     * 错误消息
     */
    @ApiModelProperty("错误消息")
    private String errorMsg;
}
