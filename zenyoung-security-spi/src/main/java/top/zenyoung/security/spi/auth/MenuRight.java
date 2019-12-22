package top.zenyoung.security.spi.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 菜单权限
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 7:08 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuRight implements Serializable {
    /**
     * 菜单代码
     */
    private Long code;
    /**
     * 菜单名称
     */
    private String name;
}
