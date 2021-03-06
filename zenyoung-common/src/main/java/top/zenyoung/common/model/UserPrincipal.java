package top.zenyoung.common.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
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
    private final Map<String, Serializable> exts = Maps.newLinkedHashMap();

    /**
     * 构造函数
     *
     * @param id      用户ID
     * @param account 用户账号
     * @param roles   用户角色集合
     * @param exts    扩展数据
     */
    public UserPrincipal(final String id, final String account, final List<String> roles, final Map<String, Serializable> exts) {
        this.id = id;
        this.account = account;
        this.roles = roles;
        if (!CollectionUtils.isEmpty(exts)) {
            this.exts.putAll(exts);
        }
    }

    /**
     * 构造函数
     *
     * @param id      用户ID
     * @param account 用户账号
     * @param roles   用户角色集合
     */
    public UserPrincipal(final String id, final String account, final List<String> roles) {
        this(id, account, roles, null);
    }

    /**
     * 构造函数
     *
     * @param principal 用户数据
     */
    public UserPrincipal(@Nonnull final UserPrincipal principal) {
        this(principal.getId(), principal.getAccount(), principal.getRoles(), principal.getExts());
    }

    /**
     * 构造函数
     */
    public UserPrincipal() {
        this(null, null, Lists.newLinkedList(), Maps.newLinkedHashMap());
    }

    /**
     * 添加数据
     *
     * @param key 数据键
     * @param val 数据值
     */
    public void add(@Nonnull final String key, @Nonnull final Serializable val) {
        getExts().put(key, val);
    }

    /**
     * 获取数据值
     *
     * @param key       数据键
     * @param dataClass 数据类型
     * @param <T>       数据泛型
     * @return 数据值
     */
    protected <T extends Serializable> T getVal(@Nonnull final String key, @Nonnull final Class<T> dataClass) {
        final Serializable obj = getExts().getOrDefault(key, null);
        if (obj != null) {
            return dataClass.cast(obj);
        }
        return null;
    }
}
