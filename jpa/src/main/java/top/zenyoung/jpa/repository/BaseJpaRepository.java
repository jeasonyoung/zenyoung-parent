package top.zenyoung.jpa.repository;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Sort;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.jpa.model.Model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * JPA-Repository 接口
 *
 * @author young
 */
public interface BaseJpaRepository<M extends Model<K>, K extends Serializable> extends BeanMapping {

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
     * @param predicate 查询条件处理
     * @return 加载数据
     */
    M getOne(@Nonnull final Predicate predicate);

    /**
     * 根据查询条件查询总记录数
     *
     * @param predicate 查询条件处理
     * @return 总记录数
     */
    long count(@Nonnull final Predicate predicate);

    /**
     * 查询数据集合
     *
     * @param predicate 查询条件处理
     * @return 数据集合
     */
    List<M> queryList(@Nonnull final Predicate predicate);

    /**
     * 分页查询数据
     *
     * @param page      分页条件
     * @param predicate 查询条件处理
     * @param sort      排序条件
     * @return 查询结果
     */
    PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Predicate predicate, @Nullable final Sort sort);

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
}
