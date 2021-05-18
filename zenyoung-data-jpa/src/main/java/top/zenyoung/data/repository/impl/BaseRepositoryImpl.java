package top.zenyoung.data.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.jpa.JpaBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 数据服务-基类
 *
 * @author yangyong
 * @version 1.0
 * 2020/2/6 4:34 下午
 **/
@Slf4j
public abstract class BaseRepositoryImpl {
    private static final int DEF_PAGING_IDX = 0, DEF_PAGING_ROWS = 10;

    /**
     * 构建分页查询
     *
     * @param pagingQuery 查询条件处理
     * @param handler     查询处理
     * @param <Qry>       查询条件类型
     * @param <Item>      查询数据类型
     * @param <Ret>       查询结果类型
     * @return 查询结果
     */
    protected <Qry extends Serializable, Item, Ret extends Serializable> PagingResult<Ret> buildPagingQuery(
            @Nullable final PagingQuery<Qry> pagingQuery,
            @Nonnull final PagingQueryHandler<Qry, Item, Ret> handler
    ) {
        int idx = DEF_PAGING_IDX, rows = DEF_PAGING_ROWS;
        Predicate predicate = null;
        if (pagingQuery != null) {
            idx = pagingQuery.getIndex() == null ? 0 : pagingQuery.getIndex() - 1;
            if (idx < 0) {
                idx = DEF_PAGING_IDX;
            }
            rows = pagingQuery.getRows() == null ? 0 : pagingQuery.getRows();
            if (rows <= 0) {
                rows = DEF_PAGING_ROWS;
            }
            predicate = handler.queryConvert(pagingQuery.getQuery());
        }
        final Page<Item> page = handler.queryData(predicate, PageRequest.of(idx, rows, handler.orderBy()));
        if (page != null) {
            final long totals = page.getTotalElements();
            final List<Ret> items = page.getContent().stream()
                    .filter(Objects::nonNull)
                    .map(handler)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return new PagingResult<>() {
                @Override
                public Long getTotal() {
                    return totals;
                }

                @Override
                public List<Ret> getRows() {
                    return items;
                }
            };
        }
        return null;
    }

    /**
     * 构建分页查询处理
     *
     * @param pagingQuery    查询条件
     * @param queryConvert   查询条件转换
     * @param orderByHandler 排序处理
     * @param jpaRepository  JPA数据接口
     * @param entityConvert  实体转换
     * @param <Qry>          查询条件类型
     * @param <Item>         数据实体类型
     * @param <Ret>          查询结果类型
     * @return 查询结果
     */
    protected <Qry extends Serializable, Item, Ret extends Serializable> PagingResult<Ret> buildPagingQuery(
            @Nullable final PagingQuery<Qry> pagingQuery,
            @Nonnull final Function<Qry, Predicate> queryConvert,
            @Nullable final Function<Qry, Sort> orderByHandler,
            @Nonnull final JpaBase<Item, ?> jpaRepository,
            @Nonnull final Function<Item, Ret> entityConvert
    ) {
        final AtomicReference<Qry> refQry = new AtomicReference<>(null);
        return buildPagingQuery(pagingQuery, new PagingQueryHandler<Qry, Item, Ret>() {

            @Override
            public Predicate queryConvert(@Nullable final Qry qry) {
                refQry.set(qry);
                return queryConvert.apply(qry);
            }

            @Override
            public Sort orderBy() {
                if (orderByHandler != null) {
                    final Sort sort = orderByHandler.apply(refQry.get());
                    if (sort != null) {
                        return sort;
                    }
                }
                return Sort.unsorted();
            }

            @Override
            public Page<Item> queryData(@Nullable final Predicate predicate, @Nonnull final Pageable pageable) {
                return predicate == null ? jpaRepository.findAll(pageable) : jpaRepository.findAll(predicate, pageable);
            }

            @Override
            public Ret apply(final Item item) {
                return entityConvert.apply(item);
            }
        });
    }

    /**
     * 构建分页查询处理
     *
     * @param pagingQuery    查询条件
     * @param queryConvert   查询条件转换
     * @param orderByHandler 排序处理
     * @param jpaRepository  JPA数据接口
     * @param entityConvert  实体转换
     * @param <Qry>          查询条件类型
     * @param <Item>         数据实体类型
     * @param <Ret>          查询结果类型
     * @return 查询结果
     */
    protected <Qry extends Serializable, Item, Ret extends Serializable> PagingResult<Ret> buildPagingQuery(
            @Nullable final PagingQuery<Qry> pagingQuery,
            @Nonnull final Function<Qry, Predicate> queryConvert,
            @Nullable final Supplier<Sort> orderByHandler,
            @Nonnull final JpaBase<Item, ?> jpaRepository,
            @Nonnull final Function<Item, Ret> entityConvert
    ) {
        return buildPagingQuery(pagingQuery, queryConvert, qry -> orderByHandler == null ? null : orderByHandler.get(), jpaRepository, entityConvert);
    }

    /**
     * 构建分页查询处理
     *
     * @param pagingQuery   查询条件
     * @param queryConvert  查询条件转换
     * @param jpaRepository JPA数据接口
     * @param entityConvert 实体转换
     * @param <Qry>         查询条件类型
     * @param <Item>        数据实体类型
     * @param <Ret>         查询结果类型
     * @return 查询结果
     */
    protected <Qry extends Serializable, Item, Ret extends Serializable> PagingResult<Ret> buildPagingQuery(
            @Nullable final PagingQuery<Qry> pagingQuery,
            @Nonnull final Function<Qry, Predicate> queryConvert,
            @Nonnull final JpaBase<Item, ?> jpaRepository,
            @Nonnull final Function<Item, Ret> entityConvert
    ) {
        return buildPagingQuery(pagingQuery, queryConvert, (Function<Qry, Sort>) null, jpaRepository, entityConvert);
    }

    /**
     * 创建排序字段处理
     *
     * @param orderBy           排序字段集合
     * @param orderFieldConvert 排序字段转换
     * @return 排序处理
     */
    protected static Sort createOrderBy(@Nullable final List<String> orderBy, @Nullable final Function<String, String> orderFieldConvert) {
        return createOrderBy(orderBy, "_", orderFieldConvert);
    }

    /**
     * 创建排序字段处理
     *
     * @param orderBy           排序字段集合
     * @param orderDirectionSep 排序方向拆分字符
     * @param orderFieldConvert 排序字段转换
     * @return 排序处理
     */
    protected static Sort createOrderBy(@Nullable final List<String> orderBy, @Nullable final String orderDirectionSep, @Nullable final Function<String, String> orderFieldConvert) {
        log.debug("createOrderBy(orderBy: {},orderDirectionSep: {},orderFieldConvert: {})...", orderBy, orderDirectionSep, orderFieldConvert);
        if (!CollectionUtils.isEmpty(orderBy)) {
            final List<Sort.Order> orders = orderBy.stream()
                    .filter(ob -> !Strings.isNullOrEmpty(ob))
                    .distinct()
                    .map(ob -> {
                        try {
                            String field = ob;
                            Sort.Direction direction = Sort.Direction.ASC;
                            if (!Strings.isNullOrEmpty(orderDirectionSep)) {
                                final int idx = ob.lastIndexOf(orderDirectionSep);
                                if (idx > 0 && idx < ob.length() - 1) {
                                    field = ob.substring(0, idx);
                                    final String d = ob.substring(idx + orderDirectionSep.length());
                                    if (!Strings.isNullOrEmpty(d)) {
                                        direction = Sort.Direction.fromOptionalString(d).orElse(Sort.Direction.ASC);
                                    }
                                }
                            }
                            if (orderFieldConvert != null) {
                                field = orderFieldConvert.apply(field);
                            }
                            return Strings.isNullOrEmpty(field) ? null : new Sort.Order(direction, field);
                        } catch (Throwable ex) {
                            log.warn("createOrderBy(orderBy: {},orderDirectionSep: {},orderFieldConvert: {})-exp: {}", orderBy, orderDirectionSep, orderFieldConvert, ex.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orders)) {
                return Sort.by(orders);
            }
        }
        return Sort.unsorted();
    }

    /**
     * 分页查询处理器
     *
     * @param <Qry>  查询类型
     * @param <Item> 数据类型
     * @param <Ret>  结果类型
     */
    protected interface PagingQueryHandler<Qry extends Serializable, Item, Ret extends Serializable> extends Function<Item, Ret> {

        /**
         * 查询条件转换
         *
         * @param qry 查询条件
         * @return 查询条件结果
         */
        Predicate queryConvert(@Nullable final Qry qry);

        /**
         * 排序处理
         *
         * @return 处理结果
         */
        default Sort orderBy() {
            return Sort.unsorted();
        }

        /**
         * 查询数据处理
         *
         * @param predicate 查询条件
         * @param pageable  分页条件
         * @return 查询结果
         */
        Page<Item> queryData(@Nullable final Predicate predicate, @Nonnull final Pageable pageable);
    }
}
