package top.zenyoung.security.spi.token;

import java.util.List;

/**
 * 令牌数据
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/10/30 2:46 下午
 **/
public interface TokenDetail {
    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    Integer getType();

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    String getUserId();

    /**
     * 获取用户账号
     *
     * @return 用户账号
     */
    String getAccount();

    /**
     * 获取角色集合
     *
     * @return 角色集合
     */
    List<String> getRoles();

    /**
     * 获取所属机构ID
     *
     * @return 所属机构ID
     */
    String getOrgId();
}
