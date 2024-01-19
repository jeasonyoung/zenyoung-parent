package top.zenyoung.data.r2dbc.service.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.SQLQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.data.entity.BaseCreateEntity;
import top.zenyoung.data.entity.Model;
import top.zenyoung.data.r2dbc.querydsl.DslUpdateClause;
import top.zenyoung.data.r2dbc.repositories.DataRepository;
import top.zenyoung.data.r2dbc.service.DataService;
import top.zenyoung.data.service.impl.BaseDataCommonServiceImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * jpa-reative 数据服务接口实现基类
 *
 * @param <M> 数据实体类型
 * @param <K> 数据主键类型
 */
@Slf4j
public abstract class BaseDataServiceImpl<M extends Model<K>, K extends Serializable>
        extends BaseDataCommonServiceImpl<M, K> implements DataService<M, K> {
    /**
     * 获取数据操作接口
     *
     * @return 数据操作接口
     */
    @Nonnull
    protected abstract DataRepository<M, K> getDataRepository();

    /**
     * QueryFactory 处理器
     *
     * @param queryHandler 业务处理器
     * @param <R>          处理结果类型
     * @return 处理结果
     */
    protected <R> RowsFetchSpec<R> queryFactoryHandler(@Nonnull final Function<SQLQuery<?>, SQLQuery<R>> queryHandler) {
        return getDataRepository().query(queryHandler);
    }

    /**
     * 业务处理器
     *
     * @param handler 业务处理器
     * @param <R>     处理结果类型
     * @return 处理结果
     */
    protected <R> R repoHandler(@Nonnull final Function<DataRepository<M, K>, R> handler) {
        return handler.apply(getDataRepository());
    }

    /**
     * 根据ID加载数据
     *
     * @param id 主键ID
     * @return 加载数据
     */
    @Nonnull
    @Override
    public Mono<M> findById(@Nonnull final K id) {
        return repoHandler(repo -> repo.findById(id));
    }

    /**
     * 根据查询条件加载数据
     *
     * @param predicate 查询条件
     * @return 加载数据
     */
    @Nonnull
    protected Mono<M> findOne(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.findOne(predicate));
    }

    /**
     * 根据查询条件加载数据
     *
     * @param where 查询条件处理
     * @return 加载数据
     */
    @Nonnull
    protected Mono<M> findOne(@Nonnull final Supplier<Predicate> where) {
        return findOne(where.get());
    }

    /**
     * 查询数据集合
     *
     * @param predicate 查询条件
     * @return 查询结果
     */
    @Nonnull
    protected Flux<M> findAll(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.findAll(predicate));
    }

    /**
     * 查询数据集合
     *
     * @param where 查询条件处理
     * @return 查询结果
     */
    @Nonnull
    protected Flux<M> findAll(@Nonnull final Supplier<Predicate> where) {
        return findAll(where.get());
    }

    /**
     * 查询数据集合并排序
     *
     * @param predicate 查询条件
     * @param sort      排序字段
     * @return 查询结果
     */
    @Nonnull
    protected Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final Sort sort) {
        return repoHandler(repo -> repo.findAll(predicate, sort));
    }

    /**
     * 查询数据集合并排序
     *
     * @param where 查询条件处理
     * @param sort  排序字段
     * @return 查询结果
     */
    @Nonnull
    protected Flux<M> findAll(@Nonnull final Supplier<Predicate> where, @Nonnull final Sort sort) {
        return findAll(where.get(), sort);
    }

    /**
     * 查询数据集合并排序
     *
     * @param predicate 查询条件
     * @param orders    排序字段
     * @return 查询结果
     */
    @Nonnull
    protected Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final OrderSpecifier<?>... orders) {
        return repoHandler(repo -> repo.findAll(predicate, orders));
    }

    /**
     * 查询数据集合并排序
     *
     * @param where  查询条件处理
     * @param orders 排序字段
     * @return 查询结果
     */
    @Nonnull
    protected Flux<M> findAll(@Nonnull final Supplier<Predicate> where, @Nonnull final OrderSpecifier<?>... orders) {
        return findAll(where.get(), orders);
    }

    /**
     * 查询排序数据
     *
     * @param orders 排序字段
     * @return 查询结果
     */
    @Nonnull
    protected Flux<M> findAll(@Nonnull final OrderSpecifier<?>... orders) {
        return repoHandler(repo -> repo.findAll(orders));
    }

    /**
     * 根据查询条件查询总记录数
     *
     * @param predicate 查询条件
     * @return 总记录数
     */
    @Nonnull
    protected Mono<Long> count(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.count(predicate));
    }

    /**
     * 根据查询条件查询总记录数
     *
     * @param where 查询条件处理
     * @return 总记录数
     */
    @Nonnull
    protected Mono<Long> count(@Nonnull final Supplier<Predicate> where) {
        return count(where.get());
    }

    /**
     * 根据查询条件查询是否存在
     *
     * @param predicate 查询条件
     * @return 是否存在
     */
    @Nonnull
    protected Mono<Boolean> exists(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.count(predicate)).map(ret -> ret > 0);
    }

    /**
     * 根据查询条件查询是否存在
     *
     * @param where 查询条件处理
     * @return 是否存在
     */
    @Nonnull
    protected Mono<Boolean> exists(@Nonnull final Supplier<Predicate> where) {
        return exists(where.get());
    }

    /**
     * 分页查询
     *
     * @param page      分页条件
     * @param predicate 查询条件
     * @param sort      排序
     * @return 查询结果
     */
    @Nonnull
    protected Mono<PageList<M>> queryForPage(@Nonnull final PagingQuery page, @Nonnull final Predicate predicate, @Nullable final Sort sort) {
        //分页
        int idx = Optional.ofNullable(page.getPageIndex()).orElse(BasePageDTO.DEF_PAGE_INDEX);
        idx = Math.max(idx - 1, 0);
        final int size = Optional.ofNullable(page.getPageSize()).orElse(BasePageDTO.DEF_PAGE_SIZE);
        //分页排序
        final Pageable pageable = sort == null ? PageRequest.of(idx, size) : PageRequest.of(idx, size, sort);
        return count(predicate)
                .flatMap(totals -> {
                    if (totals == 0) {
                        return Mono.just(PageList.empty());
                    }
                    return repoHandler(repo -> repo.findAll(predicate, pageable))
                            .collectList()
                            .map(items -> PageList.of(totals, items));
                });
    }

    /**
     * 分页查询
     *
     * @param page  分页条件
     * @param where 查询条件处理
     * @param sort  排序
     * @return 查询结果
     */
    @Nonnull
    protected Mono<PageList<M>> queryForPage(@Nonnull final PagingQuery page, @Nonnull final Supplier<Predicate> where,
                                             @Nullable final Sort sort) {
        return queryForPage(page, where.get(), sort);
    }

    /**
     * 分页查询
     *
     * @param page      分页条件
     * @param predicate 查询条件
     * @param orders    排序
     * @return 查询结果
     */
    @Nonnull
    protected Mono<PageList<M>> queryForPage(@Nonnull final PagingQuery page, @Nonnull final Predicate predicate,
                                             @Nullable final OrderSpecifier<?>... orders) {
        final Sort sort = (orders == null || orders.length == 0) ? null : new QSort(orders);
        return queryForPage(page, predicate, sort);
    }

    /**
     * 分页查询
     *
     * @param page   分页条件
     * @param where  查询条件处理
     * @param orders 排序
     * @return 查询结果
     */
    @Nonnull
    protected Mono<PageList<M>> queryForPage(@Nonnull final PagingQuery page, @Nonnull final Supplier<Predicate> where,
                                             @Nullable final OrderSpecifier<?>... orders) {
        return queryForPage(page, where.get(), orders);
    }

    /**
     * 新增数据
     *
     * @param po 新增数据
     * @return 新增结果
     */
    @Nonnull
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Mono<Boolean> add(@Nonnull final M po) {
        return repoHandler(repo -> {
            //检查ID
            if (Objects.isNull(po.getId())) {
                po.setId(genId());
            }
            //检查新增状态
            if (po instanceof BaseCreateEntity) {
                ((BaseCreateEntity<?>) po).setAddNew(true);
            }
            //新增处理
            return repo.save(po)
                    .map(Objects::nonNull);
        });
    }

    /**
     * 批量新增
     *
     * @param pos 新增数据集合
     * @return 新增结果
     */
    @Nonnull
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Mono<Boolean> batchAdd(@Nonnull final Collection<M> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return Mono.just(false);
        }
        return repoHandler(repo -> {
            //检查ID
            pos.forEach(po -> {
                //检查ID
                if (Objects.isNull(po.getId())) {
                    po.setId(genId());
                }
                //检查新增状态
                if (po instanceof BaseCreateEntity) {
                    ((BaseCreateEntity<?>) po).setAddNew(true);
                }
            });
            //批量新增处理
            return repo.saveAll(pos)
                    .collectList()
                    .map(items -> !items.isEmpty());
        });
    }

    /**
     * 修改数据
     *
     * @param updateClauseHandler 修改处理器
     * @param where               修改条件
     * @return 修改结果
     */
    @Nonnull
    protected Mono<Boolean> modify(@Nonnull final Consumer<DslUpdateClause> updateClauseHandler, @Nonnull final Predicate where) {
        return repoHandler(repo -> repo.update(updateClause -> {
            updateClauseHandler.accept(DslUpdateClause.of(updateClause));
            updateClause.where(where);
            return updateClause;
        })).map(ret -> ret > 0);
    }

    /**
     * 删除数据
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @Nonnull
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Mono<Boolean> delete(@Nonnull final K id) {
        return repoHandler(repo -> repo.deleteById(id).map(ret -> true));
    }

    /**
     * 批量删除数据
     *
     * @param ids 主键ID集合
     * @return 删除结果
     */
    @Nonnull
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Mono<Boolean> delete(@Nonnull final Collection<K> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Mono.just(false);
        }
        return repoHandler(repo -> repo.deleteAllById(ids).map(ret -> true));
    }
}
