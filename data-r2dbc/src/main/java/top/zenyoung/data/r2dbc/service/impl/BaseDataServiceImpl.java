package top.zenyoung.data.r2dbc.service.impl;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQuery;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.data.entity.BaseCreateEntity;
import top.zenyoung.data.entity.Model;
import top.zenyoung.data.r2dbc.querydsl.DslUpdateClause;
import top.zenyoung.data.r2dbc.querydsl.QuerydslR2dbc;
import top.zenyoung.data.r2dbc.querydsl.QuerydslR2dbcPredicateExecutor;
import top.zenyoung.data.r2dbc.repositories.DataRepository;
import top.zenyoung.data.r2dbc.service.DataService;
import top.zenyoung.data.service.impl.BaseDataCommonServiceImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
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
        extends BaseDataCommonServiceImpl<K> implements DataService<M, K>, QuerydslR2dbcPredicateExecutor<M> {
    private static final Map<Class<?>, EntityPath<?>> RELATIONAL_PATH_CACHE = Maps.newHashMap();
    private static final Map<Class<?>, QuerydslR2dbc> QUERYDSL_CACHE = Maps.newHashMap();

    @Autowired(required = false)
    private MySqlR2dbcQueryFactory queryFactory;
    @Autowired(required = false)
    private R2dbcMappingContext mappingContext;

    /**
     * 获取实体对象类型
     *
     * @return 实体类型
     */
    protected Class<?> getEntityType() {
        return ResolvableType.forClass(getClass().getSuperclass())
                .getGeneric(0)
                .resolve();
    }

    /**
     * 获取 QuerydslR2dbc
     *
     * @return QuerydslR2dbc
     */
    protected QuerydslR2dbc getQuerydslR2dbc() {
        return QUERYDSL_CACHE.computeIfAbsent(getClass(), k -> {
            final Class<?> entityType = getEntityType();
            if (Objects.isNull(entityType)) {
                log.warn("getEntityPath: Could not resolve query class for " + getClass());
                return null;
            }
            final RelationalPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(entityType);
            return new QuerydslR2dbc(entity);
        });
    }

    /**
     * 获取实体Path
     *
     * @return 实体Path
     */
    @SuppressWarnings({"unchecked"})
    protected EntityPath<M> getEntityPath() {
        return (EntityPath<M>) RELATIONAL_PATH_CACHE.computeIfAbsent(getClass(), k -> {
            final Class<?> entityType = getEntityType();
            if (Objects.isNull(entityType)) {
                log.warn("getEntityPath: Could not resolve query class for " + getClass());
                return null;
            }
            try {
                final String fullName = entityType.getPackage().getName() + ".Q" + entityType.getSimpleName();
                final Class<?> queryClass = entityType.getClassLoader().loadClass(fullName);
                final String fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, queryClass.getSimpleName().substring(1));
                final Field field = ReflectionUtils.findField(queryClass, fieldName);
                if (Objects.isNull(field)) {
                    log.warn("getEntityPath: Did not find a static field of the same type in " + queryClass);
                    return null;
                }
                return (EntityPath<?>) ReflectionUtils.getField(field, null);
            } catch (ClassNotFoundException e) {
                log.error("getEntityPath[entityType: {}]-exp: {}", entityType, e.getMessage());
                return null;
            }
        });
    }

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

    /**
     * 根据ID加载数据
     *
     * @param id ID
     * @return 加载数据
     */
    @Nonnull
    @Override
    public Mono<M> getById(@Nonnull final K id) {
        return repoHandler(repo -> repo.findById(id));
    }

    /**
     * 根据查询条件加载数据
     *
     * @param predicate 查询条件
     * @return 加载数据
     */
    @Nonnull
    @Override
    public Mono<M> findOne(@Nonnull final Predicate predicate) {
        return queryFactoryHandler(qf -> {
            final EntityPath<M> entityPath = getEntityPath();
            if (Objects.isNull(entityPath)) {
                return Mono.error(new ServiceException("Unable to load class entityPath"));
            }
            return qf.selectFrom(entityPath)
                    .where(predicate)
                    .fetchFirst();
        });
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
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate) {
        return queryFactoryHandler(qf -> {
            final EntityPath<M> entityPath = getEntityPath();
            if (Objects.isNull(entityPath)) {
                return Flux.error(new ServiceException("Unable to load class entityPath"));
            }
            return qf.selectFrom(entityPath)
                    .where(predicate)
                    .fetch();
        });
    }

    /**
     * 查询数据集合
     *
     * @param where 查询条件处理
     * @return 查询结果
     */
    public Flux<M> findAll(@Nonnull final Supplier<Predicate> where) {
        return findAll(where.get());
    }

    /**
     * 查询数据集合并排序
     *
     * @param predicate 查询条件
     * @param sort      排序
     * @return 查询结果
     */
    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final Sort sort) {
        return queryFactoryHandler(qf -> {
            final EntityPath<M> entityPath = getEntityPath();
            if (Objects.isNull(entityPath)) {
                return Flux.error(new ServiceException("Unable to load class entityPath"));
            }
            MySqlR2dbcQuery<M> query = qf.query()
                    .select(entityPath)
                    .from(entityPath)
                    .where(predicate);
            if (sort.isUnsorted()) {
                return query.fetch();
            }
            final QuerydslR2dbc querydsl = getQuerydslR2dbc();
            query = querydsl.applySorting(sort, query);
            return query.fetch();
        });
    }

    /**
     * 查询数据集合并排序
     *
     * @param where 查询条件处理
     * @param sort  排序
     * @return 查询结果
     */
    @Nonnull
    public Flux<M> findAll(@Nonnull final Supplier<Predicate> where, @Nonnull final Sort sort) {
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
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final OrderSpecifier<?>... orders) {
        return queryFactoryHandler(qf -> {
            final EntityPath<M> entityPath = getEntityPath();
            if (Objects.isNull(entityPath)) {
                return Flux.error(new ServiceException("Unable to load class entityPath"));
            }
            MySqlR2dbcQuery<M> query = qf.query()
                    .select(entityPath)
                    .from(entityPath)
                    .where(predicate);
            if (orders.length > 0) {
                query = query.orderBy(orders);
            }
            return query.fetch();
        });
    }

    /**
     * 查询数据集合并排序
     *
     * @param where  查询条件处理
     * @param orders 排序字段
     * @return 查询结果
     */
    public Flux<M> findAll(@Nonnull final Supplier<Predicate> where, @Nonnull final OrderSpecifier<?>... orders) {
        return findAll(where.get(), orders);
    }

    /**
     * 查询排序数据
     *
     * @param orders 排序字段
     * @return 查询结果
     */
    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final OrderSpecifier<?>... orders) {
        return queryFactoryHandler(qf -> {
            final EntityPath<M> entityPath = getEntityPath();
            if (Objects.isNull(entityPath)) {
                return Flux.error(new ServiceException("Unable to load class entityPath"));
            }
            return qf.selectFrom(entityPath)
                    .orderBy(orders)
                    .fetch();
        });
    }

    /**
     * 根据查询条件查询总记录数
     *
     * @param predicate 查询条件
     * @return 总记录数
     */
    @Nonnull
    @Override
    public Mono<Long> count(@Nullable final Predicate predicate) {
        if (Objects.isNull(predicate)) {
            return repoHandler(ReactiveCrudRepository::count);
        }
        return queryFactoryHandler(qf -> {
            final EntityPath<M> entityPath = getEntityPath();
            if (Objects.isNull(entityPath)) {
                return Mono.error(new ServiceException("Unable to load class entityPath"));
            }
            return qf.select(Wildcard.count)
                    .from(entityPath)
                    .where(predicate)
                    .fetchFirst();
        });
    }

    /**
     * 根据查询条件查询总记录数
     *
     * @param where 查询条件处理
     * @return 总记录数
     */
    @Nonnull
    public Mono<Long> count(@Nullable final Supplier<Predicate> where) {
        return count(Optional.ofNullable(where)
                .map(Supplier::get)
                .orElse(null)
        );
    }

    /**
     * 根据查询条件查询是否存在
     *
     * @param predicate 查询条件
     * @return 是否存在
     */
    @Nonnull
    @Override
    public Mono<Boolean> exists(@Nonnull final Predicate predicate) {
        return count(predicate).map(ret -> ret > 0);
    }

    /**
     * 根据查询条件查询是否存在
     *
     * @param where 查询条件处理
     * @return 是否存在
     */
    @Nonnull
    public Mono<Boolean> exists(@Nonnull final Supplier<Predicate> where) {
        return exists(where.get());
    }

    @Nonnull
    @Override
    public <S extends M, R, P extends Publisher<R>> P findBy(@Nonnull final Predicate predicate,
                                                             @Nonnull final Function<FluentQuery.ReactiveFluentQuery<S>, P> queryFunction) {
        throw new UnsupportedOperationException();
    }


    /**
     * 分页查询
     *
     * @param predicate 查询条件
     * @param pageable  分页
     * @return 查询结果
     */
    @Override
    public Mono<Page<M>> findAll(@Nullable final Predicate predicate, @Nonnull final Pageable pageable) {
        return count(predicate)
                .flatMap(count -> {
                    if (count == 0) {
                        return Mono.just(PageableExecutionUtils.getPage(Lists.newArrayList(), pageable, () -> count));
                    }
                    final EntityPath<M> entityPath = getEntityPath();
                    final QuerydslR2dbc querydsl = getQuerydslR2dbc();
                    MySqlR2dbcQuery<M> query = querydsl.applyPagination(pageable, queryFactory.query())
                            .select(entityPath)
                            .from(entityPath);
                    if (Objects.nonNull(predicate)) {
                        query = query.where(predicate);
                    }
                    return query.fetch()
                            .collectList()
                            .map(rows -> PageableExecutionUtils.getPage(rows, pageable, () -> count));
                });
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
    public Mono<PageList<M>> queryForPage(@Nullable final PagingQuery page,
                                          @Nullable final Predicate predicate,
                                          @Nullable final Sort sort) {
        //分页
        final int idx = page == null ? BasePageDTO.DEF_PAGE_INDEX : page.getPageIndex();
        final int size = page == null ? BasePageDTO.DEF_PAGE_SIZE : page.getPageSize();
        //分页排序
        final Pageable pageable = sort == null ? PageRequest.of(idx, size) : PageRequest.of(idx, size, sort);
        //分页查询
        return findAll(predicate, pageable)
                .map(p -> DataResult.of(p.getTotalElements(), p.getContent()));
    }

    /**
     * 分页查询
     *
     * @param page  分页条件
     * @param where 查询条件
     * @param sort  排序
     * @return 查询结果
     */
    @Nonnull
    public Mono<PageList<M>> queryForPage(@Nullable final PagingQuery page,
                                          @Nullable final Supplier<Predicate> where,
                                          @Nullable final Sort sort) {
        final Predicate predicate = Optional.ofNullable(where)
                .map(Supplier::get)
                .orElse(null);
        return queryForPage(page, predicate, sort);
    }

    /**
     * 分页查询
     *
     * @param page      分页条件
     * @param predicate 查询条件
     * @param orders    排序字段
     * @return 查询结果
     */
    @Nonnull
    public Mono<PageList<M>> queryForPage(@Nullable final PagingQuery page,
                                          @Nullable final Predicate predicate,
                                          @Nullable final OrderSpecifier<?>... orders) {
        final Sort sort = (orders == null || orders.length == 0) ? null : new QSort(orders);
        return queryForPage(page, predicate, sort);
    }

    /**
     * 分页查询
     *
     * @param page   分页条件
     * @param where  查询条件处理
     * @param orders 排序字段
     * @return 查询结果
     */
    @Nonnull
    public Mono<PageList<M>> queryForPage(@Nullable final PagingQuery page,
                                          @Nullable final Supplier<Predicate> where,
                                          @Nullable final OrderSpecifier<?>... orders) {
        //查询条件
        final Predicate predicate = Optional.ofNullable(where)
                .map(Supplier::get)
                .orElse(null);
        return queryForPage(page, predicate, orders);
    }

    /**
     * 新增数据
     *
     * @param po 新增数据
     * @return 新增结果
     */
    @Nonnull
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
     * @param entity              数据实体
     * @param updateClauseHandler 修改处理
     * @param where               修改条件
     * @return 更新结果
     */
    public Mono<Boolean> modify(@Nonnull final EntityPath<M> entity,
                                @Nonnull final Consumer<DslUpdateClause> updateClauseHandler,
                                @Nonnull final Predicate where) {
        return queryFactoryHandler(qf -> {
            final DslUpdateClause updateClause = DslUpdateClause.of(qf.update(entity));
            updateClauseHandler.accept(updateClause);
            return updateClause.execute(where);
        });
    }

    /**
     * 删除数据
     *
     * @param id 主键ID
     * @return 删除结果
     */
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
    @Transactional(rollbackFor = Throwable.class)
    public Mono<Boolean> delete(@Nonnull final Collection<K> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Mono.just(false);
        }
        return repoHandler(repo -> repo.deleteAllById(ids).map(ret -> true));
    }
}
