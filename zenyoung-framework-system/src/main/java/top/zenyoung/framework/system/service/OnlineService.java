package top.zenyoung.framework.system.service;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.OnlineDTO;
import top.zenyoung.framework.system.dto.OnlineQueryDTO;

import javax.annotation.Nonnull;

/**
 * 用户在线-服务接口
 *
 * @author young
 */
public interface OnlineService {

    /**
     * 用户在线-分页查询
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    PagingResult<OnlineDTO> query(@Nonnull final OnlineQueryDTO dto);

    /**
     * 用户在线-加载
     *
     * @param key 用户key
     * @return 在线用户数据
     */
    OnlineDTO getByKey(@Nonnull final String key);

    /**
     * 用户在线-批量强退
     *
     * @param keys 用户key集合
     * @return 强退结果
     */
    boolean batchForceExitByKeys(@Nonnull final String[] keys);
}
