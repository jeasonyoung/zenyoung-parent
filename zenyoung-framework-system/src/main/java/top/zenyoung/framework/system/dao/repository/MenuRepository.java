package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.MenuAddDTO;
import top.zenyoung.framework.system.dto.MenuDTO;
import top.zenyoung.framework.system.dto.MenuModifyDTO;
import top.zenyoung.framework.system.dto.MenuQueryDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 菜单-数据服务接口
 *
 * @author young
 */
public interface MenuRepository {
    /**
     * 菜单-分页查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PagingResult<MenuDTO> query(@Nonnull final MenuQueryDTO query);

    /**
     * 菜单-加载
     *
     * @param id 菜单ID
     * @return 菜单数据
     */
    MenuDTO getById(@Nonnull final Long id);

    /**
     * 菜单-全部
     *
     * @param parentId 父菜单ID
     * @return 菜单集合
     */
    List<MenuDTO> getAllByParent(@Nullable final Long parentId);

    /**
     * 菜单-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    Long add(@Nonnull final MenuAddDTO data);

    /**
     * 菜单-修改
     *
     * @param id   菜单ID
     * @param data 修改数据
     * @return 修改结果
     */
    boolean update(@Nonnull final Long id, @Nonnull final MenuModifyDTO data);

    /**
     * 菜单-删除
     *
     * @param ids 菜单ID集合
     * @return 删除结果
     */
    boolean delByIds(@Nonnull final Long[] ids);
}
