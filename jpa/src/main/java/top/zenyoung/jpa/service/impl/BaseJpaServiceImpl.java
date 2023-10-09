package top.zenyoung.jpa.service.impl;

import com.google.common.collect.Lists;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.mapping.BeanMappingDefault;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.jpa.entity.ModelEntity;
import top.zenyoung.jpa.repositories.BaseJpaRepository;
import top.zenyoung.jpa.service.JpaService;

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
public abstract class BaseJpaServiceImpl<M extends ModelEntity<K>, K extends Serializable> implements JpaService<M, K> {
    private static final BeanMapping beanMapping = BeanMappingDefault.INSTANCE;
    @Autowired(required = false)
    protected JPAQueryFactory queryFactory;

    /**
     * 获取Jpa
     *
     * @return Jpa
     */
    protected abstract BaseJpaRepository<M, K> getJpaRepository();

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

    private <R> R repoHandler(@Nonnull final Function<BaseJpaRepository<M, K>, R> handler, @Nonnull final Supplier<R> elseHandler) {
        return Optional.ofNullable(getJpaRepository())
                .map(handler)
                .orElseGet(elseHandler);
    }

    @Override
    public M getById(@Nonnull final K id) {
        return repoHandler(repo -> repo.getById(id), () -> null);
    }

    @Override
    public M getOne(@Nonnull final Predicate predicate) {
        return repoHandler(repo -> repo.findOne(predicate).orElse(null), () -> null);
    }

    @Override
    public long count(@Nullable final Predicate predicate) {
        return repoHandler(repo -> {
            if (Objects.nonNull(predicate)) {
                return repo.count(predicate);
            }
            return repo.count();
        }, () -> 0L);
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
        }, Lists::newArrayList);
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
        }, DataResult::empty);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean add(@Nonnull final M po) {
        return repoHandler(repo -> {
            repo.saveAndFlush(po);
            return true;
        }, () -> false);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean batchAdd(@Nonnull final Collection<M> pos) {
        Assert.notEmpty(pos, "'pos'不能为空");
        return repoHandler(repo -> {
            repo.saveAllAndFlush(pos);
            return true;
        }, () -> false);
    }

    /**
     * 数据更新处理
     *
     * @param entity     数据实体对象
     * @param setHandler 更新设置处理器
     * @param where      更新条件
     * @return 更新结果
     */
    protected boolean modify(@Nonnull final EntityPath<M> entity,
                             @Nonnull final Consumer<JPAUpdateClause> setHandler,
                             @Nonnull final Predicate where) {
        final JPAUpdateClause updateClause = queryFactory.update(entity);
        setHandler.accept(updateClause);
        return updateClause.where(where).execute() > 0;
    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(@Nonnull final K id) {
        return repoHandler(repo -> {
            repo.deleteById(id);
            return true;
        }, () -> false);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(@Nonnull final List<K> ids) {
        Assert.notEmpty(ids, "'ids'不能为空");
        return repoHandler(repo -> {
            repo.deleteAllById(ids);
            return true;
        }, () -> false);
    }

    /**
     * 删除数据
     *
     * @param entity 数据实体
     * @param where  删除条件
     * @return 删除结果
     */
    protected boolean delete(@Nonnull final EntityPath<M> entity, @Nonnull final Predicate where) {
        final JPADeleteClause deleteClause = queryFactory.delete(entity);
        return deleteClause.where(where).execute() > 0;
    }
}