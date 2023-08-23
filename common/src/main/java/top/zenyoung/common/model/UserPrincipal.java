package top.zenyoung.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

/**
 * 用户数据
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 4:31 下午
 **/
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPrincipal implements Principal, Serializable {
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 用户账号
     */
    private String account;
    /**
     * 用户角色集合
     */
    private Set<String> roles = Sets.newHashSet();
    /**
     * 用户权限集合
     */
    private Set<String> permissions = Sets.newHashSet();
    /**
     * 扩展数据
     */
    private Map<String, Serializable> exts = Maps.newHashMap();

    @Override
    public String getName() {
        return account + "[" + id + "]";
    }
}
