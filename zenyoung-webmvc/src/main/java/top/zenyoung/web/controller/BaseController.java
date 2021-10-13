package top.zenyoung.web.controller;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.web.AbstractWebController;
import top.zenyoung.web.ExceptHandler;
import top.zenyoung.web.controller.util.ReqUtils;
import top.zenyoung.web.listener.*;
import top.zenyoung.web.vo.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
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
public class BaseController extends AbstractWebController {

    private <ReqData, P extends PreHandlerListener<ReqData> & ExceptHandlerListener, Resp extends RespResult<?>> void handler(
            @Nullable final ReqData reqData,
            @Nonnull final Resp resp,
            @Nonnull final P listener,
            @Nonnull final Consumer<Resp> process
    ) {
        log.debug("handler(reqData: {},resp: {},listener: {},process: {})...", reqData, resp, listener, process);
        try {
            //前置业务处理
            listener.preHandler(reqData);
            //业务处理
            process.accept(resp);
        } catch (Throwable ex) {
            final String error = ReqUtils.getExceptionError(ex);
            log.warn("handler(reqData: {},resp: {},listener: {},process: {})-exp: {}", reqData, resp, listener, process, error);
            if (handlerNotExcept(resp, ex, listener)) {
                resp.buildRespFail(error);
            }
        }
    }

    private <ReqData extends Serializable, P extends PreHandlerListener<ReqData> & ExceptHandlerListener, Resp extends RespResult<?>> void handler(
            @Nonnull final Class<ReqData> reqDataClass,
            @Nonnull final Resp resp,
            @Nonnull final P listener,
            @Nonnull final Consumer<Resp> process
    ) {
        log.debug("handler(reqDataClass: {},resp: {},listener: {},process: {})...", reqDataClass, resp, listener, process);
        try {
            //请求参数处理
            final ReqData reqData = ReqUtils.parseReq(reqDataClass, this);
            //前置业务处理
            listener.preHandler(reqData);
            //业务处理
            process.accept(resp);
        } catch (Throwable ex) {
            final String error = ReqUtils.getExceptionError(ex);
            log.warn("handler(reqDataClass: {},resp: {},listener: {},process: {})-exp: {}", reqDataClass, resp, listener, process, error);
            if (handlerNotExcept(resp, ex, listener)) {
                resp.buildRespFail(error);
            }
        }
    }

    /**
     * 查询数据
     *
     * @param listener 查询处理器
     * @param <Item>   查询数据类型
     * @param <Ret>    结果数据类型
     * @return 查询结果
     */
    protected <Item, Ret extends Serializable> RespDataResult<Ret> buildQuery(
            @Nonnull final QueryListener<Item, Ret> listener
    ) {
        log.debug("buildQuery(listener: {})...", listener);
        //初始化
        final RespDataResult<Ret> respResult = RespDataResult.ofSuccess(null);
        //业务处理
        handler(null, respResult, listener, resp -> {
            //查询数据
            final List<Item> items = listener.query();
            //结果处理
            resp.buildRespSuccess(DataResult.of(
                    CollectionUtils.isEmpty(items) ? 0L : (long) items.size(),
                    CollectionUtils.isEmpty(items) ? Lists.newLinkedList() :
                            items.stream()
                                    .filter(Objects::nonNull)
                                    .map(listener)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList())
            ));
        });
        //返回数据
        return respResult;
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
     * @param reqQueryClass  查询处理器
     * @param queryHandler   查询处理
     * @param convertHandler 查询结果转换
     * @param <ReqQry>       查询条件类型
     * @param <Item>         查询数据类型
     * @param <Ret>          查询数据转换类型
     * @return 查询结果
     */
    protected <ReqQry extends Serializable, Item extends Serializable, Ret extends Serializable> RespDataResult<Ret> buildQuery(
            @Nonnull final Class<ReqQry> reqQueryClass,
            @Nonnull final Function<ReqQry, List<Item>> queryHandler,
            @Nonnull final Function<Item, Ret> convertHandler
    ) {
        return buildQuery(() -> {
            final ReqQry reqQry = ReqUtils.parseQuery(reqQueryClass, this);
            return queryHandler.apply(reqQry);
        }, convertHandler);

    }

    /**
     * 查询数据
     *
     * @param reqQueryClass       查询条件类型
     * @param queryConvertHandler 查询条件转换
     * @param queryHandler        查询处理
     * @param convertHandler      查询结果转换
     * @param <ReqQry>            查询条件类型
     * @param <Qry>               查询条件转换类型
     * @param <Item>              查询数据类型
     * @param <Ret>               查询数据转换类型
     * @return 查询结果
     */
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> RespDataResult<Ret> buildQuery(
            @Nonnull final Class<ReqQry> reqQueryClass,
            @Nonnull final Function<ReqQry, Qry> queryConvertHandler,
            @Nonnull final Function<Qry, List<Item>> queryHandler,
            @Nonnull final Function<Item, Ret> convertHandler
    ) {
        log.debug("buildQuery(reqQueryClass: {},queryConvertHandler: {},queryHandler: {},convertHandler: {})...", reqQueryClass, queryConvertHandler, queryHandler, convertHandler);
        return buildQuery(() -> {
            final ReqQry reqQry = ReqUtils.parseQuery(reqQueryClass, this);
            return queryHandler.apply(queryConvertHandler.apply(reqQry));
        }, convertHandler);
    }

    /**
     * 查询数据
     *
     * @param reqQry              查询条件
     * @param queryConvertHandler 查询条件转换
     * @param queryHandler        查询处理
     * @param convertHandler      查询结果转换
     * @param <ReqQry>            查询条件类型
     * @param <Qry>               查询条件转换类型
     * @param <Item>              查询数据类型
     * @param <Ret>               查询数据转换类型
     * @return 查询结果
     */
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> RespDataResult<Ret> buildQueryReq(
            @Nullable final ReqQry reqQry,
            @Nonnull final Function<ReqQry, Qry> queryConvertHandler,
            @Nonnull final Function<Qry, List<Item>> queryHandler,
            @Nonnull final Function<Item, Ret> convertHandler
    ) {
        log.debug("buildQuery(reqQry: {},queryConvertHandler: {},queryHandler: {},convertHandler: {})...", reqQry, queryConvertHandler, queryHandler, convertHandler);
        return buildQuery(() -> {
            final Qry query = queryConvertHandler.apply(reqQry);
            return queryHandler.apply(query);
        }, convertHandler);
    }

    /**
     * 分页查询数据
     *
     * @param reqPagingQueryClass 分页查询类型
     * @param listener            查询处理器
     * @param <ReqQry>            请求查询条件类型
     * @param <Qry>               转换后查询条件类型
     * @param <Item>              查询数据类型
     * @param <Ret>               结果数据类型
     * @return 查询结果
     */
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> RespDataResult<Ret> buildPagingQuery(
            @Nonnull final Class<ReqQry> reqPagingQueryClass,
            @Nonnull final PagingQueryListener<ReqQry, Qry, Item, Ret> listener
    ) {
        log.debug("buildQuery(reqPagingQueryClass: {},listener: {})...", reqPagingQueryClass, listener);
        //获取当前请求
        return buildPagingQuery(ReqUtils.parsePagingQuery(reqPagingQueryClass, this), listener);
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
        final RespDataResult<Ret> respResult = RespDataResult.ofSuccess(null);
        //业务处理
        handler(reqQuery.getQuery(), respResult, listener, resp -> {
            //查询数据处理
            final PagingResult<Item> queryResult = listener.query(new PagingQuery<>() {
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
            //检查数据
            final List<Item> items;
            if (queryResult == null || CollectionUtils.isEmpty(items = queryResult.getRows())) {
                resp.buildRespSuccess(DataResult.of(0L, Lists.newLinkedList()));
                return;
            }
            //结果处理
            final Long totals = queryResult.getTotal();
            resp.buildRespSuccess(DataResult.of(
                    totals == null ? 0L : totals,
                    CollectionUtils.isEmpty(items) ? Lists.newLinkedList() :
                            items.stream()
                                    .filter(Objects::nonNull)
                                    .map(listener)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList())
            ));
        });
        //返回数据
        return respResult;
    }

    /**
     * 分页查询数据
     *
     * @param reqPagingQueryClass  分页查询类型
     * @param queryConvertHandler  查询条件转换处理
     * @param pagingQueryHandler   分页查询处理
     * @param resultConvertHandler 查询结果转换处理
     * @param <ReqQry>             请求查询条件类型
     * @param <Qry>                转换后查询条件类型
     * @param <Item>               查询数据类型
     * @param <Ret>                结果数据类型
     * @return 查询结果
     */
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> RespDataResult<Ret> buildPagingQuery(
            @Nonnull final Class<ReqQry> reqPagingQueryClass,
            @Nonnull final Function<ReqQry, Qry> queryConvertHandler,
            @Nonnull final Function<PagingQuery<Qry>, PagingResult<Item>> pagingQueryHandler,
            @Nonnull final Function<Item, Ret> resultConvertHandler
    ) {
        log.debug("buildQuery(reqPagingQueryClass: {},queryConvertHandler: {},pagingQueryHandler: {},resultConvertHandler: {})...", reqPagingQueryClass, queryConvertHandler, pagingQueryHandler, resultConvertHandler);
        return buildPagingQuery(reqPagingQueryClass, new PagingQueryListener<ReqQry, Qry, Item, Ret>() {
            @Override
            public Qry convert(@Nullable final ReqQry reqQry) {
                return queryConvertHandler.apply(reqQry);
            }

            @Override
            public PagingResult<Item> query(@Nonnull final PagingQuery<Qry> query) {
                return pagingQueryHandler.apply(query);
            }

            @Override
            public Ret apply(final Item data) {
                return resultConvertHandler.apply(data);
            }
        });
    }

    /**
     * 分页查询数据
     *
     * @param reqQuery             分页查询条件
     * @param queryConvertHandler  查询条件转换处理
     * @param pagingQueryHandler   分页查询处理
     * @param resultConvertHandler 查询结果转换处理
     * @param <ReqQry>             请求查询条件类型
     * @param <Qry>                转换后查询条件类型
     * @param <Item>               查询数据类型
     * @param <Ret>                结果数据类型
     * @return 查询结果
     */
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> RespDataResult<Ret> buildPagingQuery(
            @Nonnull final PagingQuery<ReqQry> reqQuery,
            @Nonnull final Function<ReqQry, Qry> queryConvertHandler,
            @Nonnull final Function<PagingQuery<Qry>, PagingResult<Item>> pagingQueryHandler,
            @Nonnull final Function<Item, Ret> resultConvertHandler
    ) {
        log.debug("buildQuery(reqQuery: {},queryConvertHandler: {},pagingQueryHandler: {},resultConvertHandler: {})...", reqQuery, queryConvertHandler, pagingQueryHandler, resultConvertHandler);
        return buildPagingQuery(reqQuery, new PagingQueryListener<ReqQry, Qry, Item, Ret>() {
            @Override
            public Qry convert(@Nullable final ReqQry reqQry) {
                return queryConvertHandler.apply(reqQry);
            }

            @Override
            public PagingResult<Item> query(@Nonnull final PagingQuery<Qry> query) {
                return pagingQueryHandler.apply(query);
            }

            @Override
            public Ret apply(final Item data) {
                return resultConvertHandler.apply(data);
            }
        });
    }

    private <R extends Serializable, Resp extends RespResult<R>> Resp action(
            @Nonnull final Resp respResult,
            @Nonnull final ProccessListener<Void, R> listener
    ) {
        log.debug("action(respResult: {},listener: {})...", respResult, listener);
        handler(null, respResult, listener, resp -> {
            final R data = listener.apply(null);
            if (data != null) {
                resp.buildRespSuccess(data);
            }
        });
        return respResult;
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

    private <T extends Serializable, R extends Serializable, Resp extends RespResult<R>> Resp action(
            @Nonnull final Resp respResult,
            @Nullable final T req,
            @Nonnull final ProccessListener<T, R> listener
    ) {
        log.debug("action(respResult: {},req: {},listener: {})...", respResult, req, listener);
        handler(req, respResult, listener, resp -> {
            //业务处理
            final R ret = listener.apply(req);
            if (ret != null) {
                resp.buildRespSuccess(ret);
            }
        });
        return respResult;
    }

    private <T extends Serializable, R extends Serializable, Resp extends RespResult<R>> Resp action(
            @Nonnull final Resp respResult,
            @Nonnull final Class<T> reqClass,
            @Nonnull final ProccessListener<T, R> listener
    ) {
        log.debug("action(respResult: {},reqClass: {},listener: {})...", respResult, reqClass, listener);
        final AtomicReference<T> refReq = new AtomicReference<>(null);
        handler(reqClass, respResult, new ProccessListener<T, R>() {

            @Override
            public void preHandler(@Nullable final T reqData) {
                refReq.set(reqData);
                listener.preHandler(reqData);
            }

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                listener.getExceptHandlers(handlers);
            }

            @Override
            public R apply(T t) {
                return listener.apply(t);
            }
        }, resp -> {
            //业务处理
            final R ret = listener.apply(refReq.get());
            if (ret != null) {
                resp.buildRespSuccess(ret);
            }
        });
        return respResult;
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
     * 业务处理
     *
     * @param reqClass 请求数据类型
     * @param process  处理器
     * @param <T>      请求数据类型
     * @param <R>      响应数据类型
     * @return 处理结果
     */
    protected <T extends Serializable, R extends Serializable> RespResult<R> actionReq(
            @Nonnull final Class<T> reqClass,
            @Nonnull final ProccessListener<T, R> process
    ) {
        log.debug("action(reqClass: {},process: {})...", reqClass, reqClass);
        return action(RespResult.ofSuccess(null), reqClass, process);
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
        return action(RespAddResult.ofSuccess(null), req, new ProccessListener<>() {

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
        return action(RespModifyResult.ofFinish(), req, new ProccessListener<>() {

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
        return action(RespDeleteResult.ofFinish(), req, new ProccessListener<>() {

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
