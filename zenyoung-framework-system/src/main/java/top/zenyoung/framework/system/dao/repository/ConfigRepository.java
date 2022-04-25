package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.ConfigAddDTO;
import top.zenyoung.framework.system.dto.ConfigDTO;
import top.zenyoung.framework.system.dto.ConfigModifyDTO;
import top.zenyoung.framework.system.dto.ConfigQueryDTO;

import javax.annotation.Nonnull;

/**
 * 参数管理-数据操作接口
 *
 * @author young
 */
public interface ConfigRepository {

    /**
     * 参数-分页查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PagingResult<ConfigDTO> query(@Nonnull final ConfigQueryDTO query);

    /**
     * 参数-加载
     *
     * @param id 参数ID
     * @return 参数数据
     */
    ConfigDTO getById(@Nonnull final Long id);

    /**
     * 参数-新增
     *
     * @param add 新增数据
     * @return 新增结果
     */
    Long add(@Nonnull final ConfigAddDTO add);

    /**
     * 参数-更新
     *
     * @param id   参数ID
     * @param data 更新数据
     * @return 更新结果
     */
    boolean update(@Nonnull final Long id, @Nonnull final ConfigModifyDTO data);

    /**
     * 参数-删除
     *
     * @param ids 参数ID
     * @return 删除结果
     */
    boolean delByIds(@Nonnull final Long[] ids);
}
