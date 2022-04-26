package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.UserAddDTO;
import top.zenyoung.framework.system.dto.UserDTO;
import top.zenyoung.framework.system.dto.UserModifyDTO;
import top.zenyoung.framework.system.dto.UserQueryDTO;

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
}
