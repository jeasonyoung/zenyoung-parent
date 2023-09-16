package top.zenyoung.jpa.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.jpa.entity.ModelEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * JPA-Repository 接口
 *
 * @author young
 */
public interface JpaRepository<M extends ModelEntity<K>, K extends Serializable> extends BeanMapping {

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
     * 根据查询条件加载数据
     *
     * @param where 查询条件处理
     * @return 加载数据
     */
    default M getOne(@Nonnull final Supplier<Predicate> where) {
        final Predicate p = where.get();
        if (Objects.nonNull(p)) {
            return getOne(p);
        }
        return null;
    }

    /**
     * 根据查询条件查询总记录数
     *
     * @param predicate 查询条件处理
     * @return 总记录数
     */
    long count(@Nullable final Predicate predicate);

    /**
     * 根据查询条件查询总记录数
     *
     * @param where 查询条件处理
     * @return 总记录数
     */
    default long count(@Nullable final Supplier<Predicate> where) {
        return count(Optional.ofNullable(where)
                .map(Supplier::get)
                .orElse(null)
        );
    }

    /**
     * 查询数据集合
     *
     * @param predicate 查询条件处理
     * @param sort      排序条件
     * @return 数据集合
     */
    List<M> queryList(@Nullable final Predicate predicate, @Nullable final Sort sort);

    /**
     * 查询数据集合
     *
     * @param predicate 查询条件处理
     * @param orders    排序条件
     * @return 数据集合
     */
    default List<M> queryList(@Nullable final Predicate predicate, @Nullable final OrderSpecifier<?>... orders) {
        final Sort sort = (orders == null || orders.length == 0) ? null : new QSort(orders);
        return queryList(predicate, sort);
    }

    /**
     * 查询数据集合
     *
     * @param where 查询条件处理
     * @param sort  排序条件
     * @return 数据集合
     */
    default List<M> queryList(@Nullable final Supplier<Predicate> where, @Nullable final Supplier<Sort> sort) {
        //查询条件
        final Predicate p = Optional.ofNullable(where)
                .map(Supplier::get)
                .orElse(null);
        //排序
        final Sort s = Optional.ofNullable(sort)
                .map(Supplier::get)
                .orElse(null);
        //查询数据
        return queryList(p, s);
    }

    /**
     * 查询数据集合
     *
     * @param where 查询条件处理
     * @param sort  排序条件
     * @return 数据集合
     */
    default List<M> queryList(@Nullable final Supplier<Predicate> where, @Nullable final Sort sort) {
        return queryList(where, () -> sort);
    }

    /**
     * 查询数据集合
     *
     * @param where  查询条件处理
     * @param orders 排序条件
     * @return 数据集合
     */
    default List<M> queryList(@Nullable final Supplier<Predicate> where, @Nullable final OrderSpecifier<?>... orders) {
        final Predicate p = Optional.ofNullable(where)
                .map(Supplier::get)
                .orElse(null);
        return queryList(p, orders);
    }

    /**
     * 分页查询数据
     *
     * @param page      分页条件
     * @param predicate 查询条件
     * @param sort      排序条件
     * @return 查询结果
     */
    PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Predicate predicate, @Nullable final Sort sort);

    /**
     * 分页查询数据
     *
     * @param page      分页条件
     * @param predicate 查询条件
     * @param orders    排序条件
     * @return 查询结果
     */
    default PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Predicate predicate, @Nullable final OrderSpecifier<?>... orders) {
        final Sort sort = (orders == null || orders.length == 0) ? null : new QSort(orders);
        return queryForPage(page, predicate, sort);
    }

    /**
     * 分页查询数据
     *
     * @param page      分页条件
     * @param predicate 查询条件处理器
     * @param sort      排序条件处理器
     * @return 查询结果
     */
    default PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Supplier<Predicate> predicate,
                                     @Nullable final Supplier<Sort> sort) {
        //查询条件
        final Predicate p = Optional.ofNullable(predicate)
                .map(Supplier::get)
                .orElse(null);
        //排序
        final Sort s = Optional.ofNullable(sort)
                .map(Supplier::get)
                .orElse(null);
        //分页查询
        return queryForPage(page, p, s);
    }

    /**
     * 分页查询数据
     *
     * @param page      分页条件
     * @param predicate 查询条件处理器
     * @param orders    排序条件处理器
     * @return 查询结果
     */
    default PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Supplier<Predicate> predicate,
                                     @Nullable final OrderSpecifier<?>... orders) {
        return queryForPage(page, predicate, () -> (orders == null || orders.length == 0) ? null : new QSort(orders));
    }

    /**
     * 分页查询数据
     *
     * @param page      分页条件
     * @param predicate 查询条件处理器
     * @param sort      排序条件处理器
     * @return 查询结果
     */
    default PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Supplier<Predicate> predicate,
                                     @Nullable final Sort sort) {
        //查询条件
        final Predicate p = Optional.ofNullable(predicate)
                .map(Supplier::get)
                .orElse(null);
        //查询数据
        return queryForPage(page, p, sort);
    }

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
