package top.zenyoung.data.r2dbc.service.impl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQueryFactory;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.paging.DataResult;
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
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * jpa-reative 数据服务接口实现基类
 *
 * @param <M> 数据实体类型
 * @param <K> 数据主键类型
 */
public abstract class BaseDataServiceImpl<M extends Model<K>, K extends Serializable>
        extends BaseDataCommonServiceImpl<K> implements DataService<M, K>, ReactiveQuerydslPredicateExecutor<M> {
    @Autowired(required = false)
    private MySqlR2dbcQueryFactory queryFactory;

    /**
     * 获取数据操作接口
     *
     * @return 数据操作接口
     */
    protected abstract DataRepository<M, K> getDataRepository();

    /**
     * QueryFactory 处理器
     *
     * @param handler 业务处理器
     * @param <R>     处理结果类型
     * @return 处理结果
     */
    protected <R> R queryFactoryHandler(@Nonnull final Function<MySqlR2dbcQueryFactory, R> handler) {
        Assert.notNull(queryFactory, "'queryFactory'不能为空");
        return handler.apply(queryFactory);
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

    @Override
    public Mono<M> getById(@Nonnull final K id) {
        return repoHandler(repo -> repo.findById(id));
    }

    @Override
    public Mono<M> getOne(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.findOne(predicate));
    }

    @Nonnull
    @Override
    public Mono<M> findOne(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.findOne(predicate));
    }

    @Nonnull
    @Override
    public Mono<Long> count(@Nullable final Predicate predicate) {
        return repoHandler(repo -> {
            if (Objects.isNull(predicate)) {
                return repo.count();
            }
            return repo.count(predicate);
        });
    }

    @Nonnull
    @Override
    public Mono<Boolean> exists(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.exists(predicate));
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.findAll(predicate));
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final Sort sort) {
        return repoHandler(repo -> repo.findAll(predicate, sort));
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final OrderSpecifier<?>... orders) {
        return repoHandler(repo -> repo.findAll(predicate, orders));
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final OrderSpecifier<?>... orders) {
        return repoHandler(repo -> repo.findAll(orders));
    }

    @Nonnull
    @Override
    public <S extends M, R, P extends Publisher<R>> P findBy(@Nonnull final Predicate predicate,
                                                             @Nonnull final Function<FluentQuery.ReactiveFluentQuery<S>, P> queryFunction) {
        return repoHandler(repo -> repo.findBy(predicate, queryFunction));
    }

    @Override
    public Flux<M> queryList(@Nullable final Predicate predicate, @Nullable final Sort sort) {
        return repoHandler(repo -> {
            //查询条件
            if (Objects.nonNull(predicate)) {
                //排序
                if (Objects.nonNull(sort)) {
                    return repo.findAll(predicate, sort);
                }
                return repo.findAll(predicate);
            }
            //排序
            if (Objects.nonNull(sort)) {
                return repo.findAll(sort);
            }
            return repo.findAll();
        });
    }

    @Override
    public Mono<PageList<M>> queryForPage(@Nullable final PagingQuery page, @Nullable final Predicate predicate,
                                          @Nullable final Sort sort) {
        return repoHandler(repo -> {
            //分页
            final int idx = page == null ? BasePageDTO.DEF_PAGE_INDEX : page.getPageIndex();
            final int size = page == null ? BasePageDTO.DEF_PAGE_SIZE : page.getPageSize();
            //分页
            final Pageable pageable = sort == null ? PageRequest.of(idx, size) : PageRequest.of(idx, size, sort);
            //
            return repo.findAll(Objects.isNull(predicate) ? null : predicate, pageable)
                    .map(p -> DataResult.of(p.getTotalElements(), p.getContent()));
        });
    }

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

    protected Mono<Boolean> modify(@Nonnull final EntityPath<M> entity,
                                   @Nonnull final Consumer<DslUpdateClause> updateClauseHandler,
                                   @Nonnull final Predicate where) {
        return queryFactoryHandler(qf -> {
            final DslUpdateClause updateClause = DslUpdateClause.of(qf.update(entity));
            updateClauseHandler.accept(updateClause);
            return updateClause.execute(where);
        });
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Mono<Boolean> delete(@Nonnull final K id) {
        return repoHandler(repo -> repo.deleteById(id).map(ret -> true));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Mono<Boolean> delete(@Nonnull final List<K> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Mono.just(false);
        }
        return repoHandler(repo -> repo.deleteAllById(ids).map(ret -> true));
    }
}
