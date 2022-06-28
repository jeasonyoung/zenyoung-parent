package top.zenyoung.boot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zenyoung.boot.model.OperaType;
import top.zenyoung.common.model.Status;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志记录
 *
 * @author young
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperaLogDTO implements Serializable {
    /**
     * 日志主键
     */
    private Long operaId;

    /**
     * 操作模块
     */
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    private OperaType operaType;

    /**
     * 执行方法
     */
    private String method;

    /**
     * 请求url
     */
    private String reqUrl;

    /**
     * 请求方式
     */
    private String reqMethod;

    /**
     * 请求参数
     */
    private String reqParams;

    /**
     * 响应参数
     */
    private String respResult;

    /**
     * 主键值
     */
    private String primaryKeyVal;

    /**
     * 操作用时(毫秒)
     */
    private Long takeUpTime;

    /**
     * 操作人员id
     */
    private String operaUserId;

    /**
     * 操作人员姓名
     */
    private String operaUserName;

    /**
     * 操作地址
     */
    private String operaUserIpAddr;

    /**
     * 操作状态（1正常 0异常）
     */
    private Status status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private Date createTime;
}
