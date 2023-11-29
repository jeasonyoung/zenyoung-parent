package top.zenyoung.data.jpa.service.impl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.data.entity.BaseCreateEntity;
import top.zenyoung.data.entity.Model;
import top.zenyoung.data.jpa.querydsl.DslUpdateClause;
import top.zenyoung.data.jpa.repositories.DataRepository;
import top.zenyoung.data.jpa.service.DataService;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * JpaRepository 接口实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseDataServiceImpl<M extends Model<K>, K extends Serializable>
        extends BaseDataCommonServiceImpl<K> implements DataService<M, K>, QuerydslPredicateExecutor<M> {
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
        return repoHandler(repo -> repo.getReferenceById(id));
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

    @Override
    public PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Predicate predicate, @Nullable final Sort sort) {
        return repoHandler(repo -> {
            //分页
            final int idx = page == null ? BasePageDTO.DEF_PAGE_INDEX : page.getPageIndex();
            final int size = page == null ? BasePageDTO.DEF_PAGE_SIZE : page.getPageSize();
            //分页
            final Pageable pageable = sort == null ? PageRequest.of(idx, size) : PageRequest.of(idx, size, sort);
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
    public boolean delete(@Nonnull final List<K> ids) {
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
}