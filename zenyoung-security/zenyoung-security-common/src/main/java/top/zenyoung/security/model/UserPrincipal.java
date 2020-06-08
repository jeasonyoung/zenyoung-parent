package top.zenyoung.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 用户数据
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 4:31 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Serializable {
    /**
     * 用户ID
     */
    private String id;
    /**
     * 用户账号
     */
    private String account;
    /**
     * 用户角色集合
     */
    private List<String> roles;

    /**
     * 扩展数据
     */
    private Map<String, Serializable> exts;
}
