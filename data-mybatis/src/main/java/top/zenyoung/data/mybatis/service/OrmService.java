package top.zenyoung.data.mybatis.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.data.entity.Model;

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
public interface OrmService<M extends Model<K>, K extends Serializable> extends BeanMapping {
    /**
     * 根据ID加载数据
     *
     * @param id ID
     * @return 加载数据
     */
    M getById(@Nonnull final K id);

    /**
     * 根据查询条件加载数据
     *
     * @param consumer 查询条件处理
     * @return 加载数据
     */
    M getOne(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer);

    /**
     * 根据查询条件加载数据
     *
     * @param query 查询条件
     * @return 加载数据
     */
    M getOne(@Nonnull final Wrapper<M> query);

    /**
     * 根据查询条件查询总记录数
     *
     * @param consumer 查询条件处理
     * @return 总记录数
     */
    int count(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer);

    /**
     * 根据查询条件查询总记录数
     *
     * @param query 查询条件
     * @return 总记录数
     */
    int count(@Nonnull final Wrapper<M> query);

    /**
     * 查询数据集合
     *
     * @param consumer 查询条件处理
     * @return 数据集合
     */
    List<M> queryList(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer);

    /**
     * 查询数据集合
     *
     * @param query 查询条件
     * @return 数据集合
     */
    List<M> queryList(@Nonnull final Wrapper<M> query);

    /**
     * 分页查询数据
     *
     * @param page     分页条件
     * @param consumer 查询条件处理
     * @return 查询结果
     */
    PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Consumer<LambdaQueryWrapper<M>> consumer);

    /**
     * 分页查询数据
     *
     * @param page  分页条件
     * @param query 查询条件
     * @return 查询结果
     */
    PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Wrapper<M> query);

    /**
     * 新增
     *
     * @param po 新增数据
     * @return 新增结果
     */
    boolean add(@Nonnull final M po);

    /**
     * 批量新增
     *
     * @param items 新增数据集合
     * @return 新增结果
     */
    boolean batchAdd(@Nonnull final Collection<M> items);

    /**
     * 根据ID修改数据
     *
     * @param id ID
     * @param po 修改数据
     * @return 修改结果
     */
    boolean modify(@Nonnull final K id, @Nonnull final M po);

    /**
     * 根据条件更新数据
     *
     * @param consumer 更新数据条件处理
     * @return 更新结果
     */
    boolean modify(@Nonnull final Consumer<LambdaUpdateWrapper<M>> consumer);

    /**
     * 根据条件更新数据
     *
     * @param updateWrapper 更新数据条件
     * @return 更新结果
     */
    boolean modify(@Nonnull final LambdaUpdateWrapper<M> updateWrapper);

    /**
     * 批量更新数据
     *
     * @param items 数据集合
     * @return 更新结果
     */
    boolean batchModify(@Nonnull final Collection<M> items);

    /**
     * 根据主键ID删除
     *
     * @param id 主键ID
     * @return 删除
     */
    boolean delete(@Nonnull final K id);

    /**
     * 根据主键ID集合删除
     *
     * @param ids 主键ID集合
     * @return 删除
     */
    boolean delete(@Nonnull final List<K> ids);

    /**
     * 根据条件删除数据
     *
     * @param consumer 删除条件处理
     * @return 删除结果
     */
    boolean delete(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer);

    /**
     * 根据条件删除数据
     *
     * @param wrapper 删除条件
     * @return 删除结果
     */
    boolean delete(@Nonnull final Wrapper<M> wrapper);
}
