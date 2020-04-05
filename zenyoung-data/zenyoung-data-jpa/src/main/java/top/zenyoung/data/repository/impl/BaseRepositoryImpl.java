package top.zenyoung.data.repository.impl;

import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据服务-基类
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/6 4:34 下午
 **/
@Slf4j
public abstract class BaseRepositoryImpl {
    private static final int DEF_PAGING_IDX = 0, DEF_PAGING_ROWS = 10;

    /**
     * 构造分页查询
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
            return new PagingResult<Ret>() {
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
