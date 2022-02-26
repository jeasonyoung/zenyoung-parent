package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import top.zenyoung.framework.system.dao.converter.MenuTypeConverter;
import top.zenyoung.framework.system.model.MenuType;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 菜单-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_sys_menu")
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_menu set status = -1 where id = ?")
public class MenuEntity extends BaseStatusEntity {
    /**
     * 菜单代码(排序)
     */
    @Column(nullable = false)
    private Integer code;
    /**
     * 菜单名称
     */
    @Column(length = 128, nullable = false)
    private String name;
    /**
     * 父菜单ID
     */
    @Column
    private Long parentId;

    /**
     * 路由地址
     */
    @Column
    private String path;
    /**
     * 组件路径
     */
    @Column
    private String component;
    /**
     * 路由参数
     */
    @Column
    private String query;
    /**
     * 是否为外链(0:否,1:是)
     */
    @Column
    private Integer isLink = 0;
    /**
     * 是否缓存(0:不缓存,1:缓存)
     */
    @Column
    private Integer isCache = 0;
    /**
     * 菜单类型(1:目录,2:菜单,3:按钮)
     */
    @Column(nullable = false)
    @Convert(converter = MenuTypeConverter.class)
    private MenuType type = MenuType.Dir;
    /**
     * 菜单状态(1:显示,0:隐藏)
     */
    @Column
    private Integer visible = 1;
    /**
     * 权限标识
     */
    @Column
    private String perms;
    /**
     * 菜单图标
     */
    @Column(length = 128)
    private String icon = "#";
}
