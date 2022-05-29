package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.LoginLogAddDTO;
import top.zenyoung.framework.system.dto.LoginLogDTO;
import top.zenyoung.framework.system.dto.LoginLogDelDTO;
import top.zenyoung.framework.system.dto.LoginLogQueryDTO;

import javax.annotation.Nonnull;

/**
 * 登录日志-数据操作接口
 *
 * @author young
 */
public interface LoginLogRepository {
    /**
     * 登录日志-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PagingResult<LoginLogDTO> query(@Nonnull final LoginLogQueryDTO query);

    /**
     * 登录日志-加载
     *
     * @param id 登录日志ID
     * @return 加载数据
     */
    LoginLogDTO getById(@Nonnull final Long id);

    /**
     * 登录日志-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    Long add(@Nonnull final LoginLogAddDTO data);

    /**
     * 登录日志-批量删除
     *
     * @param dto 删除条件
     * @return 删除数据量
     */
    boolean batchDels(@Nonnull final LoginLogDelDTO dto);
}
