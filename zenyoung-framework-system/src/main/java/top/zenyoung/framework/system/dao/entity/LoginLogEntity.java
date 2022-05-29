package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 系统访问记录-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_sys_login_log")
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_login_log set status = -1 where id = ?")
public class LoginLogEntity extends BaseStatusEntity {
    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;
    /**
     * 用户账号
     */
    @Column(nullable = false)
    private String account;
    /**
     * 登录IP地址
     */
    @Column(length = 32)
    private String ipAddr;
    /**
     * 登录地点
     */
    @Column(length = 128)
    private String ipLocation;
    /**
     * 浏览器类型
     */
    @Column(length = 128)
    private String browser;
    /**
     * 操作系统
     */
    @Column(length = 128)
    private String os;
    /**
     * 客户端设备
     */
    @Column(length = 128)
    private String device;
    /**
     * 提示消息
     */
    @Column
    private String msg;
}
