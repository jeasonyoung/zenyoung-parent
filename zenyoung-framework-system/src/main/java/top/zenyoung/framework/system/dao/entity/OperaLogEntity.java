package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import top.zenyoung.framework.common.OperaBizType;
import top.zenyoung.framework.system.dao.converter.OperaBizTypeConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 操作记录-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_sys_opera_log")
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_opera_log set status = -1 where id = ?")
public class OperaLogEntity extends BaseStatusEntity {
    /**
     * 模块标题
     */
    @Column(length = 128)
    private String title;
    /**
     * 业务类型(0:查询,1:新增,2:修改,3:删除,4:其它)
     */
    @Column
    @Convert(converter = OperaBizTypeConverter.class)
    private OperaBizType type = OperaBizType.Query;
    /**
     * 方法名称
     */
    @Column
    private String method;
    /**
     * 请求URL
     */
    @Column(length = 512)
    private String url;
    /**
     * 请求方式
     */
    @Column(length = 32)
    private String reqMethod;

    /**
     * 操作人员
     */
    @Column(length = 64)
    private String operaName;
    /**
     * 操作IP地址
     */
    @Column(length = 32)
    private String operaIp;
    /**
     * 操作地址
     */
    @Column(length = 128)
    private String operaLocation;
    /**
     * 操作设备
     */
    @Column(length = 128)
    private String operaDevice;
    /**
     * 请求参数
     */
    @Column
    private String operaParam;
    /**
     * 返回参数
     */
    @Column
    private String operaResult;
    /**
     * 错误消息
     */
    @Column
    private String errorMsg;
}
