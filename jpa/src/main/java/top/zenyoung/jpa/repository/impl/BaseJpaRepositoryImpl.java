package top.zenyoung.jpa.repository.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.mapping.BeanMappingDefault;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.jpa.jpa.BaseJpa;
import top.zenyoung.jpa.model.Model;
import top.zenyoung.jpa.repository.BaseJpaRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * JpaRepository 接口实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseJpaRepositoryImpl<M extends Model<K>, K extends Serializable> implements BaseJpaRepository<M, K> {
    private static final BeanMapping beanMapping = BeanMappingDefault.INSTANCE;
    private final Map<Integer, Class<?>> clsMaps = Maps.newConcurrentMap();
    @Autowired(required = false)
    protected JPAQueryFactory queryFactory;
    @Autowired(required = false)
    private IdSequence idSequence;

    private Class<?> getGenericKeyType() {
        final Class<?> cls = getClass();
        final int index = 1;
        return clsMaps.computeIfAbsent(index, idx -> {
            // 直接使用 spring 静态方法，减少对象创建
            final Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(cls, BaseJpaRepositoryImpl.class);
            return typeArguments == null ? null : typeArguments[idx];
        });
    }

    /**
     * 生成主键ID
     *
     * @return 主键ID
     */
    @SuppressWarnings({"unchecked"})
    protected K genId() {
        return Optional.ofNullable(idSequence)
                .map(idSeq -> {
                    final Long id = idSeq.nextId();
                    final Class<K> cls = (Class<K>) getGenericKeyType();
                    if (cls == Long.class) {
                        return cls.cast(id);
                    }
                    if (cls == String.class) {
                        return cls.cast(String.valueOf(id));
                    }
                    return cls.cast(id);
                })
                .orElse(null);
    }

    /**
     * 获取Jpa
     *
     * @return Jpa
     */
    protected abstract BaseJpa<M, K> getJpa();

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
    public M getById(@Nonnull final K id) {
        return Optional.ofNullable(getJpa())
                .map(jpa -> jpa.getById(id))
                .orElse(null);
    }

    @Override
    public M getOne(@Nonnull final Predicate predicate) {
        return Optional.ofNullable(getJpa())
                .flatMap(jpa -> jpa.findOne(predicate))
                .orElse(null);
    }

    @Override
    public long count(@Nonnull final Predicate predicate) {
        return Optional.ofNullable(getJpa())
                .map(jpa -> jpa.count(predicate))
                .orElse(0L);
    }

    @Override
    public List<M> queryList(@Nonnull final Predicate predicate) {
        return Optional.ofNullable(getJpa())
                .map(jpa -> jpa.findAll(predicate))
                .map(iter -> StreamSupport.stream(iter.spliterator(), false)
                        .distinct()
                        .collect(Collectors.toList())
                )
                .orElse(Lists.newArrayList());
    }

    @Override
    public PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Predicate predicate, @Nullable final Sort sort) {
        return Optional.ofNullable(getJpa())
                .map(jpa -> {
                    final int idx = page == null ? BasePageDTO.DEF_PAGE_INDEX : page.getPageIndex();
                    final int size = page == null ? BasePageDTO.DEF_PAGE_SIZE : page.getPageSize();
                    final Pageable pageable = sort == null ? PageRequest.of(idx, size) : PageRequest.of(idx, size, sort);
                    final Page<M> p = predicate == null ? jpa.findAll(pageable) : jpa.findAll(predicate, pageable);
                    return DataResult.of(p.getTotalElements(), p.getContent());
                })
                .orElse(DataResult.empty());
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean add(@Nonnull final M po) {
        if (Objects.isNull(po.getId())) {
            po.setId(genId());
        }
        return Optional.ofNullable(getJpa())
                .map(jpa -> {
                    jpa.saveAndFlush(po);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean batchAdd(@Nonnull final Collection<M> items) {
        items.forEach(item -> {
            if (Objects.isNull(item.getId())) {
                item.setId(genId());
            }
        });
        return Optional.ofNullable(getJpa())
                .map(jpa -> {
                    jpa.saveAllAndFlush(items);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(@Nonnull final K id) {
        return Optional.ofNullable(getJpa())
                .map(jpa -> {
                    jpa.deleteById(id);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(@Nonnull final List<K> ids) {
        return Optional.ofNullable(getJpa())
                .map(jpa -> {
                    jpa.deleteAllById(ids);
                    return true;
                })
                .orElse(false);
    }
}