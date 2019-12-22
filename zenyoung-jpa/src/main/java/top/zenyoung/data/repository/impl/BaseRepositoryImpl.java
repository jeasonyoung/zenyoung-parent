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
 * 数据服务基类
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/10/18 2:54 下午
 **/
@Slf4j
public class BaseRepositoryImpl {
    private static final int DEF_PAGING_INDEX = 0, DEF_PAGING_ROWS = 10;

    /**
     * 构建分页查询
     *
     * @param pagingQuery 分页查询条件
     * @param listener    查询处理监听器
     * @param <Qry>       查询类型
     * @param <Item>      数据类型
     * @param <Ret>       结果类型
     * @return 查询结果
     */
     protected <Qry extends Serializable, Item, Ret extends Serializable> PagingResult<Ret> buildPagingQueryResult(@Nullable final PagingQuery<Qry> pagingQuery, @Nonnull final QueryListener<Qry, Item, Ret> listener) {
         log.debug("buildPagingQueryResult(pagingQuery: {},listener: {})...", pagingQuery, listener);
         int idx = DEF_PAGING_INDEX, rows = DEF_PAGING_ROWS;
         Predicate predicate = null;
         if (pagingQuery != null) {
             //页码
             idx = (pagingQuery.getIndex() == null ? 0 : pagingQuery.getIndex()) - 1;
             if (idx < 0) {
                 idx = DEF_PAGING_INDEX;
             }
             //每页数据
             rows = pagingQuery.getRows() == null ? 0 : pagingQuery.getRows();
             if (rows <= 0) {
                 rows = DEF_PAGING_ROWS;
             }
             //查询条件转换
             predicate = listener.queryConvert(pagingQuery.getQuery());
         }
         //查询数据处理
         final Page<Item> page = listener.queryData(predicate, PageRequest.of(idx, rows, listener.orderBy()));
         if (page != null) {
             final long totals = page.getTotalElements();
             final List<Ret> rets = page.getContent().stream()
                     .filter(Objects::nonNull)
                     .map(listener)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
             //
             return new PagingResult<Ret>() {

                 @Override
                 public Long getTotals() {
                     return totals;
                 }

                 @Override
                 public List<Ret> getRows() {
                     return rets;
                 }
             };
         }
         return null;
     }

    /**
     * 查询处理监听器
     *
     * @param <Qry>  查询类型
     * @param <Item> 数据类型
     * @param <Ret>  结果类型
     */
    protected interface QueryListener<Qry extends Serializable, Item, Ret extends Serializable> extends Function<Item, Ret> {

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
         * @return 排序结果
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
