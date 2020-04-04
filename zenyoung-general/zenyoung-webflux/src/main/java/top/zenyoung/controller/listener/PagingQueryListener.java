package top.zenyoung.controller.listener;

import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.function.Function;

/**
 * 分页查询监听器
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/7 2:11 下午
 **/
public interface PagingQueryListener<ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> extends Function<Item, Ret>, PreHandlerListener<ReqQry>, ExceptHandlerListener {

    /**
     * 查询条件转换
     *
     * @param reqQry 转换前查询条件
     * @return 转换后查询条件
     */
    Qry convert(@Nullable final ReqQry reqQry);

    /**
     * 查询处理
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PagingResult<Item> query(@Nonnull final PagingQuery<Qry> query);
}
