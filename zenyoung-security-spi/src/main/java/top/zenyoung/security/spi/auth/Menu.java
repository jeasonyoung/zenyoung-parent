package top.zenyoung.security.spi.auth;

import com.google.common.collect.Lists;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 7:12 下午
 **/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Menu extends MenuRight implements Serializable {
    /**
     * 菜单ID
     */
    private String id;
    /**
     * 菜单路径
     */
    private String path;
    /**
     * 菜单组件名
     */
    private String component;
    /**
     * 菜单图标
     */
    private String iconCls;
    /**
     * 菜单权限集合
     */
    private List<MenuRight> rights = Lists.newLinkedList();
    /**
     * 子菜单集合
     */
    private List<Menu> children = Lists.newLinkedList();

    /**
     * 构造函数
     *
     * @param id        菜单ID
     * @param code      菜单代码
     * @param name      菜单名称
     * @param path      菜单路径
     * @param component 菜单组件
     * @param iconCls   菜单图标
     * @param rights    菜单权限集合
     * @param children  子菜单集合
     */
    @Builder
    public Menu(final String id, final Long code, final String name,
                final String path, final String component, final String iconCls,
                final List<MenuRight> rights, final List<Menu> children) {
        super(code, name);
        this.id = id;
        this.path = path;
        this.component = component;
        this.iconCls = iconCls;
        this.rights = rights;
        this.children = children;
    }
}
