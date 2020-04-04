package top.zenyoung.security.spi.auth;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 登录认证管理接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/11/1 9:28 上午
 **/
public interface UserDetailService {

    /**
     * 根据用户账号获取认证数据
     *
     * @param account 用户账号
     * @return 用户认证数据
     */
    UserDetail getAuthenByAccount(@Nonnull final String account);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfo getUserInfoById(@Nonnull final String userId);

    /**
     * 根据用户ID加载用户菜单集合
     *
     * @param userId 用户ID
     * @return 用户菜单集合
     */
    List<Menu> getMenusByUserId(@Nonnull final String userId);

    /**
     * 绑定用户处理
     *
     * @param userId   用户ID
     * @param bindType 绑定类型
     * @param bindId   绑定ID
     */
    void bindUserHandler(@Nonnull final String userId, @Nullable final Integer bindType, @Nullable final String bindId);
}