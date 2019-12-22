package top.zenyoung.security.spi.auth;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单元数据
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 7:10 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuMeta implements Serializable {
    /**
     * 是否生命周期保持
     */
    private boolean keepAlive;
    /**
     * 是否强制认证
     */
    private boolean requireAuth;
    /**
     * 菜单角色
     */
    private List<String> roles = Lists.newLinkedList();
}
