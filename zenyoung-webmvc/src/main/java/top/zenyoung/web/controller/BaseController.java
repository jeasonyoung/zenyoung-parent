package top.zenyoung.web.controller;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.web.ExceptHandler;
import top.zenyoung.web.listener.*;
import top.zenyoung.web.vo.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 控制器-基类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/10 9:53 下午
 **/
@Slf4j
public class BaseController {
    /**
     * 查询数据
     *
     * @param listener 查询处理器
     * @param <Item>   查询数据类型
     * @param <Ret>    结果数据类型
     * @return 查询结果
     */
    protected <Item, Ret extends Serializable> RespDataResult<Ret> buildQuery(@Nonnull final QueryListener<Item, Ret> listener) {
        log.debug("buildQuery(listener: {})...", listener);
        //初始化
        final RespDataResult<Ret> resp = RespDataResult.ofSuccess(null);
        try {
            //查询处理
            final List<Item> items = listener.query();
            final boolean isEmpty = CollectionUtils.isEmpty(items);
            resp.buildRespSuccess(DataResult.of(
                    isEmpty ? 0L : (long) items.size(),
                    isEmpty ? Lists.newLinkedList() : items.stream()
                            .filter(Objects::nonNull).map(listener)
                            .filter(Objects::nonNull).collect(Collectors.toList())
            ));
        } catch (Throwable ex) {
            log.error("buildQuery(listener: {})-exp: {}", listener, ex.getMessage());
            resp.buildRespFail(ex.getMessage());
        }
        //返回数据
        return resp;
    }

    /**
     * 查询数据
     *
     * @param queryHandler   查询处理器
     * @param convertHandler 查询结果转换处理器
     * @param <Item>         查询数据类型
     * @param <Ret>          结果数据类型
     * @return 查询结果
     */
    protected <Item, Ret extends Serializable> RespDataResult<Ret> buildQuery(
            @Nonnull final Supplier<List<Item>> queryHandler,
            @Nonnull final Function<Item, Ret> convertHandler
    ) {
        log.debug("buildQuery(queryHandler: {},convertHandler: {})...", queryHandler, convertHandler);
        return buildQuery(new QueryListener<Item, Ret>() {
            @Override
            public List<Item> query() {
                return queryHandler.get();
            }

            @Override
            public Ret apply(final Item data) {
                return convertHandler.apply(data);
            }
        });
    }

    /**
     * 查询数据
     *
     * @param queryConvertHandler 查询条件转换
     * @param queryHandler        查询处理
     * @param convertHandler      查询结果转换
     * @param <Qry>               查询条件转换类型
     * @param <Item>              查询数据类型
     * @param <Ret>               查询数据转换类型
     * @return 查询结果
     */
    protected <Qry extends Serializable, Item extends Serializable, Ret extends Serializable> RespDataResult<Ret> buildQuery(
            @Nonnull final Supplier<Qry> queryConvertHandler,
            @Nonnull final Function<Qry, List<Item>> queryHandler,
            @Nonnull final Function<Item, Ret> convertHandler
    ) {
        log.debug("buildQuery(queryConvertHandler: {},queryHandler: {},convertHandler: {})...", queryConvertHandler, queryHandler, convertHandler);
        return buildQuery(() -> queryHandler.apply(queryConvertHandler.get()), convertHandler);
    }

    /**
     * 分页查询数据
     *
     * @param reqQuery 分页查询条件
     * @param listener 查询处理器
     * @param <ReqQry> 请求查询条件类型
     * @param <Qry>    转换后查询条件类型
     * @param <Item>   查询数据类型
     * @param <Ret>    结果数据类型
     * @return 查询结果
     */
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> RespDataResult<Ret> buildPagingQuery(
            @Nonnull final PagingQuery<ReqQry> reqQuery,
            @Nonnull final PagingQueryListener<ReqQry, Qry, Item, Ret> listener
    ) {
        log.debug("buildQuery(reqQuery: {},listener: {})...", reqQuery, listener);
        //初始化
        final RespDataResult<Ret> resp = RespDataResult.ofSuccess(null);
        try {
            //查询条件处理
            final PagingResult<Item> queryResult = listener.query(new PagingQuery<Qry>() {
                @Override
                public Integer getIndex() {
                    return reqQuery.getIndex();
                }

                @Override
                public Integer getRows() {
                    return reqQuery.getRows();
                }

                @Override
                public Qry getQuery() {
                    return listener.convert(reqQuery.getQuery());
                }
            });
            //查询数据处理
            final Long totals = queryResult.getTotal();
            if (totals == null || totals <= 0) {
                resp.buildRespSuccess(DataResult.of(0L, Lists.newLinkedList()));
            } else {
                final List<Item> items = queryResult.getRows();
                final boolean isEmpty = CollectionUtils.isEmpty(items);
                resp.buildRespSuccess(DataResult.of(totals,
                        isEmpty ? Lists.newLinkedList() :
                                items.stream().filter(Objects::nonNull).map(listener).filter(Objects::nonNull).collect(Collectors.toList())
                ));
            }
        } catch (Throwable ex) {
            log.error("buildPagingQuery(reqQuery: {},listener: {})-exp: {}", reqQuery, listener, ex.getMessage());
            resp.buildRespFail(ex.getMessage());
        }
        return resp;
    }

    /**
     * 分页查询数据
     *
     * @param queryConvertHandler  查询条件转换处理
     * @param pagingQueryHandler   分页查询处理
     * @param resultConvertHandler 查询结果转换处理
     * @param <Qry>                转换后查询条件类型
     * @param <Item>               查询数据类型
     * @param <Ret>                结果数据类型
     * @return 查询结果
     */
    protected <Qry extends Serializable, Item extends Serializable, Ret extends Serializable> RespDataResult<Ret> buildPagingQuery(
            @Nonnull final Supplier<PagingQuery<Qry>> queryConvertHandler,
            @Nonnull final Function<PagingQuery<Qry>, PagingResult<Item>> pagingQueryHandler,
            @Nonnull final Function<Item, Ret> resultConvertHandler
    ) {
        log.debug("buildQuery(queryConvertHandler: {},pagingQueryHandler: {},resultConvertHandler: {})...", queryConvertHandler, pagingQueryHandler, resultConvertHandler);
        return buildPagingQuery(queryConvertHandler.get(), new PagingQueryListener<Qry, Qry, Item, Ret>() {
            @Override
            public Qry convert(@Nullable final Qry qry) {
                return qry;
            }

            @Override
            public PagingResult<Item> query(@Nonnull final PagingQuery<Qry> query) {
                return pagingQueryHandler.apply(query);
            }

            @Override
            public Ret apply(final Item item) {
                return resultConvertHandler.apply(item);
            }
        });
    }

    private <T extends Serializable, R extends Serializable, Resp extends RespResult<R>> Resp action(
            @Nonnull final Resp respResult,
            @Nullable final T req,
            @Nonnull final ProccessListener<T, R> listener
    ) {
        log.debug("action(respResult: {},req: {},listener: {})...", respResult, req, listener);
        try {
            respResult.buildRespSuccess(listener.apply(req));
        } catch (Throwable ex) {
            log.error("action(respResult: {},req: {},listener: {})-exp: {}", respResult, req, listener, ex.getMessage());
            respResult.buildRespFail(ex.getMessage());
        }
        return respResult;
    }

    private <R extends Serializable, Resp extends RespResult<R>> Resp action(
            @Nonnull final Resp respResult,
            @Nonnull final ProccessListener<Void, R> listener
    ) {
        log.debug("action(respResult: {},listener: {})...", respResult, listener);
        return action(respResult, null, vod -> null);
    }

    /**
     * 业务处理-无入参验证
     *
     * @param listener 处理器
     * @param <R>      返回数据类型
     * @return 处理结果
     */
    protected <R extends Serializable> RespResult<R> action(@Nonnull final ProccessListener<Void, R> listener) {
        return action(RespResult.ofSuccess(null), listener);
    }

    /**
     * 业务处理
     *
     * @param req     请求数据
     * @param process 处理器
     * @param <T>     请求数据类型
     * @param <R>     响应数据类型
     * @return 处理结果
     */
    protected <T extends Serializable, R extends Serializable> RespResult<R> action(
            @Nullable final T req,
            @Nonnull final ProccessListener<T, R> process
    ) {
        log.debug("action(req: {},process: {})...", req, process);
        return action(RespResult.ofSuccess(null), req, process);
    }

    /**
     * 业务处理-新增
     *
     * @param req      请求数据
     * @param proccess 处理器
     * @param <T>      请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> RespAddResult actionAdd(
            @Nullable final T req,
            @Nonnull final ProccessListener<T, Serializable> proccess
    ) {
        log.debug("actionAdd(req: {},process: {})...", req, proccess);
        return action(RespAddResult.ofSuccess(null), req, new ProccessListener<T, RespAddResult.AddResult>() {

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                proccess.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final T reqData) {
                proccess.preHandler(reqData);
            }

            @Override
            public RespAddResult.AddResult apply(final T data) {
                return RespAddResult.AddResult.of(proccess.apply(data));
            }
        });
    }

    /**
     * 业务处理-修改
     *
     * @param req      请求数据
     * @param proccess 处理器
     * @param <T>      请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> RespModifyResult actionModify(
            @Nullable final T req,
            @Nonnull final ProccessModifyListener<T> proccess
    ) {
        log.debug("actionModify(req: {},proccess: {})...", req, proccess);
        return action(RespModifyResult.ofFinish(), req, new ProccessListener<T, Serializable>() {

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                proccess.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final T reqData) {
                proccess.preHandler(reqData);
            }

            @Override
            public Serializable apply(final T data) {
                proccess.accept(data);
                return null;
            }
        });
    }

    /**
     * 业务处理-修改
     *
     * @param proccess 处理器
     * @return 处理结果
     */
    protected RespModifyResult actionModify(@Nonnull final ProccessModifyListener<Void> proccess) {
        log.debug("actionModify(proccess: {})...", proccess);
        return action(RespModifyResult.ofFinish(), new ProccessListener<Void, Serializable>() {

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                proccess.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final Void reqData) {
                proccess.preHandler(reqData);
            }

            @Override
            public Serializable apply(final Void data) {
                proccess.accept(data);
                return null;
            }
        });
    }

    /**
     * 业务处理-删除
     *
     * @param req      请求数据
     * @param proccess 业务处理器
     * @param <T>      请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> RespDeleteResult actionDelete(
            @Nullable final T req,
            @Nonnull final ProccessModifyListener<T> proccess
    ) {
        log.debug("actionDelete(req: {},proccess: {})...", req, proccess);
        return action(RespDeleteResult.ofFinish(), req, new ProccessListener<T, Serializable>() {

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                proccess.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final T reqData) {
                proccess.preHandler(reqData);
            }

            @Override
            public Serializable apply(final T data) {
                proccess.accept(data);
                return null;
            }
        });
    }

    /**
     * 业务处理-删除
     *
     * @param proccess 删除处理器
     * @return 处理结果
     */
    protected RespDeleteResult actionDelete(@Nonnull final ProccessDeleteListener proccess) {
        log.debug("actionDelete(proccess: {})...", proccess);
        return action(RespDeleteResult.ofFinish(), new ProccessListener<Void, Serializable>() {

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                proccess.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final Void reqData) {
                proccess.preHandler(reqData);
            }

            @Override
            public Serializable apply(final Void aVoid) {
                proccess.accept(aVoid);
                return null;
            }
        });
    }
}
