package top.zenyoung.orm.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import top.zenyoung.boot.service.BaseService;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.orm.model.BasePO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * ORM-操作服务接口
 *
 * @author young
 */
public interface BaseOrmService<PO extends BasePO<ID>, ID extends Serializable> extends BaseService {
    /**
     * 根据ID加载数据
     *
     * @param id ID
     * @return 加载数据
     */
    PO getById(@Nonnull final ID id);

    /**
     * 根据查询条件加载数据
     *
     * @param consumer 查询条件处理
     * @return 加载数据
     */
    PO getOne(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer);

    /**
     * 根据查询条件加载数据
     *
     * @param query 查询条件
     * @return 加载数据
     */
    PO getOne(@Nonnull final Wrapper<PO> query);

    /**
     * 根据查询条件查询总记录数
     *
     * @param consumer 查询条件处理
     * @return 总记录数
     */
    int count(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer);

    /**
     * 根据查询条件查询总记录数
     *
     * @param query 查询条件
     * @return 总记录数
     */
    int count(@Nonnull final Wrapper<PO> query);

    /**
     * 查询数据集合
     *
     * @param consumer 查询条件处理
     * @return 数据集合
     */
    List<PO> queryList(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer);

    /**
     * 查询数据集合
     *
     * @param query 查询条件
     * @return 数据集合
     */
    List<PO> queryList(@Nonnull final Wrapper<PO> query);

    /**
     * 分页查询数据
     *
     * @param page     分页条件
     * @param consumer 查询条件处理
     * @return 查询结果
     */
    PageList<PO> queryForPage(@Nullable final PagingQuery page, @Nullable final Consumer<LambdaQueryWrapper<PO>> consumer);

    /**
     * 分页查询数据
     *
     * @param page  分页条件
     * @param query 查询条件
     * @return 查询结果
     */
    PageList<PO> queryForPage(@Nullable final PagingQuery page, @Nullable final Wrapper<PO> query);

    /**
     * 新增
     *
     * @param po 新增数据
     * @return 新增结果
     */
    PO add(@Nonnull final PO po);

    /**
     * 批量新增
     *
     * @param items 新增数据集合
     * @return 新增结果
     */
    boolean batchAdd(@Nonnull final Collection<PO> items);

    /**
     * 根据ID修改数据
     *
     * @param id ID
     * @param po 修改数据
     * @return 修改结果
     */
    int modify(@Nonnull final ID id, @Nonnull final PO po);

    /**
     * 根据条件更新数据
     *
     * @param consumer 更新数据条件处理
     * @return 更新结果
     */
    int modify(@Nonnull final Consumer<LambdaUpdateWrapper<PO>> consumer);

    /**
     * 根据条件更新数据
     *
     * @param updateWrapper 更新数据条件
     * @return 更新结果
     */
    int modify(@Nonnull final LambdaUpdateWrapper<PO> updateWrapper);

    /**
     * 批量更新数据
     *
     * @param items 数据集合
     * @return 更新结果
     */
    boolean batchModify(@Nonnull final Collection<PO> items);

    /**
     * 根据主键ID删除
     *
     * @param id 主键ID
     * @return 删除
     */
    boolean delete(@Nonnull final ID id);

    /**
     * 根据主键ID集合删除
     *
     * @param ids 主键ID集合
     * @return 删除
     */
    boolean delete(@Nonnull final Collection<ID> ids);

    /**
     * 根据条件删除数据
     *
     * @param consumer 删除条件处理
     * @return 删除结果
     */
    boolean delete(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer);

    /**
     * 根据条件删除数据
     *
     * @param wrapper 删除条件
     * @return 删除结果
     */
    boolean delete(@Nonnull final Wrapper<PO> wrapper);
}
