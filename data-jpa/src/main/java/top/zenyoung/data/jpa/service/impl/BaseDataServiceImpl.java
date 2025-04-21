package top.zenyoung.data.jpa.service.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.data.entity.BaseCreateEntity;
import top.zenyoung.data.entity.Model;
import top.zenyoung.data.jpa.querydsl.DslUpdateClause;
import top.zenyoung.data.jpa.repositories.DataRepository;
import top.zenyoung.data.jpa.service.DataService;
import top.zenyoung.data.jpa.util.SpringContextUtils;
import top.zenyoung.data.service.impl.BaseDataCommonServiceImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * JpaRepository 接口实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseDataServiceImpl<M extends Model<K>, K extends Serializable>
        extends BaseDataCommonServiceImpl<M, K> implements DataService<M, K>, QuerydslPredicateExecutor<M> {
    /**
     * queryFactory实体
     */
    @Autowired(required = false)
    private JPAQueryFactory queryFactory;

    /**
     * QueryFactory 处理器
     *
     * @param handler 业务处理器
     * @param <R>     处理结果类型
     * @return 处理结果
     */
    protected <R> R queryFactoryHandler(@Nonnull final Function<JPAQueryFactory, R> handler) {
        Assert.notNull(queryFactory, "'queryFactory'不能为空");
        return handler.apply(queryFactory);
    }

    /**
     * 获取数据操作接口
     *
     * @return 数据操作接口
     */
    @Nonnull
    protected abstract DataRepository<M, K> getDataRepository();

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
    public M getById(@Nonnull final K id) {
        return repoHandler(repo -> repo.findById(id).orElse(null));
    }

    @Override
    public M getOne(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.findOne(predicate).orElse(null));
    }

    @Nonnull
    @Override
    public Optional<M> findOne(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.findOne(predicate));
    }

    @Nonnull
    @Override
    public Iterable<M> findAll(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.findAll(predicate));
    }

    @Nonnull
    @Override
    public Iterable<M> findAll(@Nonnull final Predicate predicate, @Nonnull final Sort sort) {
        return repoHandler(repo -> repo.findAll(predicate, sort));
    }

    @Nonnull
    @Override
    public Iterable<M> findAll(@Nonnull final Predicate predicate, @Nonnull final OrderSpecifier<?>... orders) {
        return repoHandler(repo -> repo.findAll(predicate, orders));
    }

    @Nonnull
    @Override
    public Iterable<M> findAll(@Nonnull final OrderSpecifier<?>... orders) {
        return repoHandler(repo -> repo.findAll(orders));
    }

    @Nonnull
    @Override
    public Page<M> findAll(@Nonnull final Predicate predicate, @Nonnull final Pageable pageable) {
        return repoHandler(repo -> repo.findAll(predicate, pageable));
    }

    @Override
    public boolean exists(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.exists(predicate));
    }

    @Nonnull
    @Override
    public <S extends M, R> R findBy(@Nonnull final Predicate predicate, @Nonnull final Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return repoHandler(repo -> repo.findBy(predicate, queryFunction));
    }

    @Override
    public long count(@Nullable final Predicate predicate) {
        return repoHandler(repo -> {
            if (Objects.nonNull(predicate)) {
                return repo.count(predicate);
            }
            return repo.count();
        });
    }

    @Override
    public List<M> queryList(@Nullable final Predicate predicate, @Nullable final Sort sort) {
        return repoHandler(repo -> {
            if (Objects.nonNull(predicate)) {
                final Function<Iterable<M>, List<M>> handler = iter ->
                        StreamSupport.stream(iter.spliterator(), false)
                                .distinct()
                                .collect(Collectors.toList());
                if (Objects.nonNull(sort)) {
                    return handler.apply(repo.findAll(predicate, sort));
                }
                return handler.apply(repo.findAll(predicate));
            }
            if (Objects.nonNull(sort)) {
                return repo.findAll(sort);
            }
            return repo.findAll();
        });
    }

    /**
     * 构建分页对象
     *
     * @param page 分页数据接口
     * @param sort 排序对象
     * @return 分页对象
     */
    protected Pageable buildPageable(@Nullable final PagingQuery page, @Nullable final Sort sort) {
        //分页
        final int idx = Math.max((page == null ? BasePageDTO.DEF_PAGE_INDEX : page.getPageIndex()) - 1, 0);
        final int size = page == null ? BasePageDTO.DEF_PAGE_SIZE : page.getPageSize();
        //分页
        return sort == null ? PageRequest.of(idx, size) : PageRequest.of(idx, size, sort);
    }

    @Override
    public PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Predicate predicate, @Nullable final Sort sort) {
        return repoHandler(repo -> {
            //分页
            final Pageable pageable = buildPageable(page, sort);
            //查询条件
            final Page<M> p = predicate == null ? repo.findAll(pageable) : repo.findAll(predicate, pageable);
            //分页查询
            return DataResult.of(p.getTotalElements(), p.getContent());
        });
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean add(@Nonnull final M po) {
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
            repo.saveAndFlush(po);
            return true;
        });
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean batchAdd(@Nonnull final Collection<M> pos) {
        Assert.notEmpty(pos, "'pos'不能为空");
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
            repo.saveAllAndFlush(pos);
            return true;
        });
    }

    /**
     * 数据更新处理
     *
     * @param entity              数据实体对象
     * @param updateClauseHandler 更新设置处理
     * @param where               更新条件
     * @return 更新结果
     */
    protected boolean modify(@Nonnull final EntityPath<M> entity,
                             @Nonnull final Consumer<DslUpdateClause> updateClauseHandler,
                             @Nonnull final Predicate where) {
        return queryFactoryHandler(qf -> {
            final DslUpdateClause updateClause = DslUpdateClause.of(qf.update(entity));
            updateClauseHandler.accept(updateClause);
            //更新人处理
            final var updatedByField = ReflectionUtils.findField(entity.getClass(), "updatedBy", StringPath.class);
            if (Objects.nonNull(updatedByField)) {
                final AuditorAware<?> auditor = SpringContextUtils.getBean(AuditorAware.class);
                if (Objects.nonNull(auditor)) {
                    final String current = (String) auditor.getCurrentAuditor().orElse(null);
                    if (!Strings.isNullOrEmpty(current)) {
                        final StringPath updatedBy = (StringPath) ReflectionUtils.getField(updatedByField, entity);
                        if (Objects.nonNull(updatedBy)) {
                            updateClause.add(updatedBy, current);
                        }
                    }
                }
            }
            return updateClause.execute(where);
        });
    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(@Nonnull final K id) {
        return repoHandler(repo -> {
            repo.deleteById(id);
            return true;
        });
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(@Nonnull final Collection<K> ids) {
        Assert.notEmpty(ids, "'ids'不能为空");
        return repoHandler(repo -> {
            repo.deleteAllById(ids);
            return true;
        });
    }

    /**
     * 删除数据
     *
     * @param entity 数据实体
     * @param where  删除条件
     * @return 删除结果
     */
    protected boolean delete(@Nonnull final EntityPath<M> entity, @Nonnull final Predicate where) {
        return queryFactoryHandler(qf -> {
            final JPADeleteClause deleteClause = qf.delete(entity);
            return deleteClause.where(where).execute() > 0;
        });
    }

    /**
     * 拼接Where条件
     *
     * @param oldWhere 旧条件
     * @param newWhere 新条件
     * @return 拼接后条件
     */
    protected BooleanExpression join(@Nullable final BooleanExpression oldWhere, @Nonnull final BooleanExpression newWhere) {
        return oldWhere == null ? newWhere : oldWhere.and(newWhere);
    }

    /**
     * 拼接Where条件
     *
     * @param oldWhere        旧条件
     * @param newWhereHandler 新条件处理器
     * @return 拼接后条件
     */
    protected BooleanExpression join(@Nullable final BooleanExpression oldWhere, @Nonnull final Supplier<BooleanExpression> newWhereHandler) {
        final BooleanExpression newWhere = newWhereHandler.get();
        return oldWhere == null ? newWhere : oldWhere.and(newWhere);
    }
}