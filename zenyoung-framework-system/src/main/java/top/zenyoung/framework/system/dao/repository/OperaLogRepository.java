package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.OperaLogAddDTO;
import top.zenyoung.framework.system.dto.OperaLogDTO;
import top.zenyoung.framework.system.dto.OperaLogDelDTO;
import top.zenyoung.framework.system.dto.OperaLogQueryDTO;

import javax.annotation.Nonnull;

/**
 * 操作记录-数据服务接口
 *
 * @author young
 */
public interface OperaLogRepository {

    /**
     * 操作记录-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PagingResult<OperaLogDTO> query(@Nonnull final OperaLogQueryDTO query);

    /**
     * 操作记录-加载
     *
     * @param id 操作记录ID
     * @return 加载数据
     */
    OperaLogDTO getById(@Nonnull final Long id);

    /**
     * 操作记录-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    Long add(@Nonnull final OperaLogAddDTO data);

    /**
     * 操作记录-批量删除
     *
     * @param dto 删除条件
     * @return 删除结果
     */
    boolean batchDel(@Nonnull final OperaLogDelDTO dto);
}
