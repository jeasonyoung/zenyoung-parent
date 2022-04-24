package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.ConfigDTO;
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

}
