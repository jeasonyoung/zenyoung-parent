package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.RoleAddDTO;
import top.zenyoung.framework.system.dto.RoleDTO;
import top.zenyoung.framework.system.dto.RoleModifyDTO;
import top.zenyoung.framework.system.dto.RoleQueryDTO;

import javax.annotation.Nonnull;

/**
 * 角色-数据服务接口
 *
 * @author young
 */
public interface RoleRepository {
    /**
     * 角色-分页查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PagingResult<RoleDTO> query(@Nonnull final RoleQueryDTO query);

    /**
     * 角色-加载
     *
     * @param id 角色ID
     * @return 角色数据
     */
    RoleDTO getById(@Nonnull final Long id);

    /**
     * 角色-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    Long add(@Nonnull final RoleAddDTO data);

    /**
     * 角色-更新
     *
     * @param id   角色ID
     * @param data 更新数据
     * @return 更新结果
     */
    boolean update(@Nonnull final Long id, @Nonnull final RoleModifyDTO data);

    /**
     * 角色-删除
     *
     * @param ids 角色ID集合
     * @return 删除结果
     */
    boolean delByIds(@Nonnull final Long[] ids);
}
