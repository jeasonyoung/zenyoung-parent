package top.zenyoung.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.web.AbstractWebController;
import top.zenyoung.web.ExceptHandler;
import top.zenyoung.web.listener.*;
import top.zenyoung.web.vo.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 控制器-基类
 *
 * @author yangyong
 * @version 1.0
 * 2020/2/7 12:28 下午
 **/
@Slf4j
public abstract class BaseController extends AbstractWebController {

    private <ReqData, P extends PreHandlerListener<ReqData> & ExceptHandlerListener, Resp extends RespResult<?>> void handler(
            @Nullable final MonoSink<Resp> sink,
            @Nullable final ReqData reqData,
            @Nonnull final Resp resp,
            @Nonnull final P listener,
            @Nonnull final Consumer<Resp> process
    ) {
        try {
            //前置业务处理
            listener.preHandler(reqData);
            //业务处理
            process.accept(resp);
        } catch (Throwable e) {
            log.warn("handler-exp:", e);
            if (handlerNotExcept(resp, e, listener)) {
                resp.buildRespFail(e.getMessage());
            }
        } finally {
            if (sink != null) {
                sink.success(resp);
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
    protected <Item extends Serializable, Ret extends Serializable> Mono<RespDataResult<Ret>> buildQuery(
            @Nonnull final QueryListener<Item, Ret> listener
    ) {
        return Mono.create(sink -> handler(sink, null, RespDataResult.<Ret>ofSuccess(null), listener,
                resp -> {
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
                })
        );
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
    protected <Item extends Serializable, Ret extends Serializable> Mono<RespDataResult<Ret>> buildQuery(
            @Nonnull final Supplier<List<Item>> queryHandler,
            @Nonnull final Function<Item, Ret> convertHandler
    ) {
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
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> Mono<RespDataResult<Ret>> buildQuery(
            @Nonnull final PagingQuery<ReqQry> reqQuery,
            @Nonnull final PagingQueryListener<ReqQry, Qry, Item, Ret> listener
    ) {
        return Mono.create(sink -> handler(sink, reqQuery.getQuery(), RespDataResult.<Ret>ofSuccess(null), listener,
                resp -> {
                    //查询数据处理
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
                    if (queryResult == null || CollectionUtils.isEmpty(queryResult.getRows())) {
                        resp.buildRespSuccess(DataResult.of(0L, Lists.newLinkedList()));
                        return;
                    }
                    //查询结果处理
                    final Long totals = queryResult.getTotal();
                    final List<Item> items = queryResult.getRows();
                    resp.buildRespSuccess(DataResult.of(
                            totals == null ? 0L : totals,
                            CollectionUtils.isEmpty(items) ? Lists.newLinkedList() :
                                    items.stream()
                                            .filter(Objects::nonNull)
                                            .map(listener)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList())
                    ));
                }
        ));
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
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> Mono<RespDataResult<Ret>> buildQuery(
            @Nonnull final PagingQuery<ReqQry> reqQuery,
            @Nonnull final Function<ReqQry, Qry> queryConvertHandler,
            @Nonnull final Function<PagingQuery<Qry>, PagingResult<Item>> pagingQueryHandler,
            @Nonnull final Function<Item, Ret> resultConvertHandler
    ) {
        return buildQuery(reqQuery, new PagingQueryListener<ReqQry, Qry, Item, Ret>() {
            @Override
            public Qry convert(@Nullable ReqQry reqQry) {
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

    private <R extends Serializable, Resp extends RespResult<R>> Mono<Resp> action(
            @Nonnull final Resp resp,
            @Nonnull final ProccessListener<Void, R> listener
    ) {
        return Mono.create(sink -> handler(sink, null, resp, listener,
                respRet -> {
                    final R data = listener.apply(null);
                    if (data != null) {
                        respRet.buildRespSuccess(data);
                    }
                }
        ));
    }

    private String actionExceptionHandler(@Nullable final Throwable throwable) {
        if (throwable != null) {
            if (throwable instanceof BindingResult) {
                final String error = ((BindingResult) throwable).getFieldErrors().stream()
                        .map(fe -> {
                            String err = fe.getDefaultMessage();
                            if (Strings.isNullOrEmpty(err)) {
                                err = fe.toString();
                            }
                            return err;
                        })
                        .filter(err -> !Strings.isNullOrEmpty(err))
                        .collect(Collectors.joining());
                if (!Strings.isNullOrEmpty(error)) {
                    return error;
                }
            }
            return throwable.getMessage();
        }
        return null;
    }

    /**
     * 业务处理-无入参验证
     *
     * @param listener 处理器
     * @param <R>      返回数据类型
     * @return 处理结果
     */
    protected <R extends Serializable> Mono<RespResult<R>> action(@Nonnull final ProccessListener<Void, R> listener) {
        return action(RespResult.ofSuccess(null), listener);
    }

    private <T extends Serializable, R extends Serializable, Resp extends RespResult<R>> Mono<Resp> action(
            @Nonnull final Mono<T> req,
            @Nonnull final Supplier<Resp> respSuccessHandler,
            @Nonnull final Function<String, Resp> respFailHandler,
            @Nonnull final ProccessListener<T, R> listener
    ) {
        return Mono.create(sink -> req.doOnError(Throwable.class, e -> sink.success(respFailHandler.apply(actionExceptionHandler(e))))
                .doOnNext(data -> handler(sink, data, respSuccessHandler.get(), listener,
                        respRet -> {
                            //业务处理
                            final R ret = listener.apply(data);
                            if (ret != null) {
                                respRet.buildRespSuccess(ret);
                            }
                        }
                )).subscribe()
        );
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
    protected <T extends Serializable, R extends Serializable> Mono<RespResult<R>> action(
            @Nonnull final Mono<T> req,
            @Nonnull final ProccessListener<T, R> process
    ) {
        return action(req, () -> RespResult.ofSuccess(null), RespResult::ofFail, process);
    }

    /**
     * 业务处理-新增
     *
     * @param req     请求数据
     * @param process 处理器
     * @param <T>     请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> Mono<RespAddResult> actionAdd(
            @Nonnull final Mono<T> req,
            @Nonnull final ProccessListener<T, String> process
    ) {
        return action(req, () -> RespAddResult.ofSuccess(null), err -> RespAddResult.of(ResultCode.Fail, err, null),
                new ProccessListener<T, RespAddResult.AddResult>() {
                    @Override
                    public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                        process.getExceptHandlers(handlers);
                    }

                    @Override
                    public void preHandler(@Nullable final T reqData) {
                        process.preHandler(reqData);
                    }

                    @Override
                    public RespAddResult.AddResult apply(final T data) {
                        return RespAddResult.AddResult.of(process.apply(data));
                    }
                }
        );
    }

    /**
     * 业务处理-修改
     *
     * @param req     请求数据
     * @param process 处理器
     * @param <T>     请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> Mono<RespModifyResult> actionModify(
            @Nonnull final Mono<T> req,
            @Nonnull final ProccessModifyListener<T> process
    ) {
        return action(req, RespModifyResult::ofFinish, err -> RespModifyResult.of(ResultCode.Fail, err),
                new ProccessListener<T, Serializable>() {

                    @Override
                    public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                        process.getExceptHandlers(handlers);
                    }

                    @Override
                    public void preHandler(@Nullable final T reqData) {
                        process.preHandler(reqData);
                    }

                    @Override
                    public Serializable apply(final T data) {
                        process.accept(data);
                        return null;
                    }
                }
        );
    }

    /**
     * 业务处理-删除
     *
     * @param req     请求数据
     * @param process 业务处理器
     * @param <T>     请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> Mono<RespDeleteResult> actionDelete(
            @Nonnull final Mono<T> req,
            @Nonnull final ProccessModifyListener<T> process
    ) {
        return action(req, RespDeleteResult::ofFinish, err -> RespDeleteResult.of(ResultCode.Fail, err),
                new ProccessListener<T, Serializable>() {

                    @Override
                    public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                        process.getExceptHandlers(handlers);
                    }

                    @Override
                    public void preHandler(@Nullable final T reqData) {
                        process.preHandler(reqData);
                    }

                    @Override
                    public Serializable apply(final T data) {
                        process.accept(data);
                        return null;
                    }
                }
        );
    }

    /**
     * 业务处理-删除
     *
     * @param process 删除处理器
     * @return 处理结果
     */
    protected Mono<RespDeleteResult> actionDelete(@Nonnull final ProccessDeleteListener process) {
        return action(RespDeleteResult.ofFinish(),
                new ProccessListener<Void, Serializable>() {

                    @Override
                    public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                        process.getExceptHandlers(handlers);
                    }

                    @Override
                    public void preHandler(@Nullable final Void reqData) {
                        process.preHandler(reqData);
                    }

                    @Override
                    public Serializable apply(final Void aVoid) {
                        //删除处理器
                        process.accept(aVoid);
                        return null;
                    }
                }
        );
    }
}