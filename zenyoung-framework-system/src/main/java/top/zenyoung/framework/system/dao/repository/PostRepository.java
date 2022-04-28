package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.PostAddDTO;
import top.zenyoung.framework.system.dto.PostDTO;
import top.zenyoung.framework.system.dto.PostModifyDTO;
import top.zenyoung.framework.system.dto.PostQueryDTO;

import javax.annotation.Nonnull;

/**
 * 岗位-数据服务接口
 *
 * @author young
 */
public interface PostRepository {
    /**
     * 岗位-分页查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PagingResult<PostDTO> query(@Nonnull final PostQueryDTO query);

    /**
     * 岗位-加载
     *
     * @param id 用户ID
     * @return 用户数据
     */
    PostDTO getById(@Nonnull final Long id);

    /**
     * 岗位-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    Long add(@Nonnull final PostAddDTO data);

    /**
     * 岗位-更新
     *
     * @param id   岗位ID
     * @param data 岗位数据
     * @return 更新结果
     */
    boolean update(@Nonnull final Long id, @Nonnull final PostModifyDTO data);

    /**
     * 岗位-删除
     *
     * @param ids 岗位ID
     * @return 删除结果
     */
    boolean delByIds(@Nonnull final Long[] ids);
}
