package top.zenyoung.common.model;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.security.Principal;
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
public class UserPrincipal implements Principal, Serializable {
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
     * 设备标识
     */
    private String device;
    /**
     * 扩展数据
     */
    private Map<String, Serializable> exts = Maps.newLinkedHashMap();

    @Override
    public String getName() {
        return account + "[" + id + "]";
    }

    /**
     * 构造函数
     *
     * @param id      用户ID
     * @param account 用户账号
     * @param roles   用户角色集合
     * @param device  用户设备标识
     * @param exts    扩展数据
     */
    public UserPrincipal(final String id, final String account, final List<String> roles, final String device,
                         final Map<String, Serializable> exts) {
        this.id = id;
        this.account = account;
        this.roles = roles;
        this.device = device;
        if (exts != null && exts.size() > 0) {
            this.exts.putAll(exts);
        }
    }

    /**
     * 构造函数
     *
     * @param id      用户ID
     * @param account 用户账号
     * @param roles   用户角色集合
     * @param device  设备标识
     */
    public UserPrincipal(final String id, final String account, final List<String> roles, final String device) {
        this(id, account, roles, device, null);
    }

    /**
     * 构造函数
     *
     * @param principal 用户数据
     */
    public UserPrincipal(@Nonnull final UserPrincipal principal) {
        this(principal.getId(), principal.getAccount(), principal.getRoles(), principal.getDevice(), principal.getExts());
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
