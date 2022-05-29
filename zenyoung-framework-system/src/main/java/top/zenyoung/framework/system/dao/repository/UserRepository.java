package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.auth.AuthUser;
import top.zenyoung.framework.system.dto.*;

import javax.annotation.Nonnull;

/**
 * 用户-数据服务接口
 *
 * @author young
 */
public interface UserRepository {

    /**
     * 用户-分页查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PagingResult<UserDTO> query(@Nonnull final UserQueryDTO query);

    /**
     * 用户-加载
     *
     * @param id 用户ID
     * @return 加载数据
     */
    UserDTO getById(@Nonnull final Long id);

    /**
     * 用户-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    Long add(@Nonnull final UserAddDTO data);

    /**
     * 用户-更新
     *
     * @param id   用户ID
     * @param data 更新数据
     * @return 更新结果
     */
    boolean update(@Nonnull final Long id, @Nonnull final UserModifyDTO data);

    /**
     * 用户-删除
     *
     * @param ids 用户ID
     * @return 删除结果
     */
    boolean delByIds(@Nonnull final Long[] ids);

    /**
     * 用户-查找用户
     *
     * @param account 登录用户
     * @return 认证用户信息
     */
    AuthUser findByAccount(@Nonnull final String account);

    /**
     * 用户-重置密码
     *
     * @param userId 用户ID
     * @param data   重置密码数据
     * @return 重置结果
     */
    boolean restPassword(@Nonnull final Long userId, @Nonnull final UserRestPasswordDTO data);
}
