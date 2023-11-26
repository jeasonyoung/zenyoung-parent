package top.zenyoung.jpa.reactive.service.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.mapping.BeanMappingDefault;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.jpa.reactive.repositories.BaseJpaReactiveRepository;
import top.zenyoung.jpa.reactive.service.JpaReactiveService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * jpa-reative 数据服务接口实现基类
 *
 * @param <M> 数据实体类型
 * @param <K> 数据主键类型
 */
public abstract class BaseJpaReactiveServiceImpl<M extends Serializable, K extends Serializable>
        implements JpaReactiveService<M, K>, ReactiveQuerydslPredicateExecutor<M> {
    private static final BeanMapping beanMapping = BeanMappingDefault.INSTANCE;

    @Autowired(required = false)
    private MySqlR2dbcQueryFactory queryFactory;

    /**
     * 获取数据操作接口
     *
     * @return 数据操作接口
     */
    protected abstract BaseJpaReactiveRepository<M, K> getJpaRepository();

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
    protected <R> R repoHandler(@Nonnull final Function<BaseJpaReactiveRepository<M, K>, R> handler) {
        return handler.apply(getJpaRepository());
    }

    @Override
    public <T, R> R mapping(@Nullable final T data, @Nonnull final Class<R> cls) {
        return beanMapping.mapping(data, cls);
    }

    @Override
    public <T, R> List<R> mapping(@Nullable final List<T> items, @Nonnull final Class<R> cls) {
        return beanMapping.mapping(items, cls);
    }

    @Override
    public <T extends Serializable, R extends Serializable> PageList<R> mapping(@Nullable final PageList<T> pageList, @Nonnull final Class<R> cls) {
        return beanMapping.mapping(pageList, cls);
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
    public Mono<Long> count(@Nullable final Predicate predicate) {
        return repoHandler(repo -> {
            if (Objects.isNull(predicate)) {
                return repo.count();
            }
            return repo.count(predicate);
        });
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
        return repoHandler(repo -> repo.save(po).map(Objects::nonNull));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Mono<Boolean> batchAdd(@Nonnull final Collection<M> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return Mono.just(false);
        }
        return repoHandler(repo -> repo.saveAll(pos).collectList().map(items -> !items.isEmpty()));
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
