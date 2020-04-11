package top.zenyoung.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import top.zenyoung.common.model.DataResult;
import top.zenyoung.common.model.RespDataResult;
import top.zenyoung.common.model.RespResult;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.controller.listener.*;
import top.zenyoung.controller.model.ExceptHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 控制器-基类
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/7 12:28 下午
 **/
@Slf4j
public abstract class BaseController {

    /**
     * 获取异常处理器集合
     *
     * @return 异常处理器集合
     */
    @Nonnull
    protected List<ExceptHandler> getExceptHandlers() {
        return Lists.newLinkedList();
    }

    protected boolean handlerNotExcept(
            @Nonnull final RespResult<?> respResult,
            @Nullable final Throwable e,
            @Nonnull final ExceptHandlerListener listener
    ) {
        //获取异常处理器集合
        final List<ExceptHandler> exceptHandlers = getExceptHandlers();
        if (exceptHandlers.size() > 0) {
            //添加异常处理集合
            listener.getExceptHandlers(exceptHandlers);
        }
        if (e != null && exceptHandlers.size() > 0) {
            final Map<Class<? extends Throwable>, ExceptHandler> handlerMap = exceptHandlers.stream()
                    .collect(Collectors.toMap(ExceptHandler::getEClass, handler -> handler, (n, o) -> n));
            if (handlerMap.size() > 0) {
                return handlerNotExceptCause(respResult, e, handlerMap);
            }
        }
        return true;
    }

    private boolean handlerNotExceptCause(
            @Nonnull final RespResult<?> respResult,
            @Nonnull final Throwable e,
            @Nonnull final Map<Class<? extends Throwable>, ExceptHandler> handlerMap
    ) {
        final ExceptHandler handler = handlerMap.getOrDefault(e.getClass(), null);
        if (handler != null) {
            respResult.setCode(handler.getCode());
            respResult.setMsg(e.getMessage());
            return false;
        }
        final Throwable cause = e.getCause();
        if (cause != null) {
            return handlerNotExceptCause(respResult, cause, handlerMap);
        }
        return true;
    }

    protected <ReqData, P extends PreHandlerListener<ReqData> & ExceptHandlerListener, Resp extends RespResult<?>> void handler(
            @Nullable final ReqData reqData,
            @Nonnull final Resp resp,
            @Nonnull final P listener,
            @Nonnull final Consumer<Resp> handler
    ) {
        try {
            //前置业务处理
            listener.preHandler(reqData);
            //业务处理
            handler.accept(resp);
        } catch (Throwable e) {
            log.warn("handler-exp:", e);
            if (handlerNotExcept(resp, e, listener)) {
                resp.buildRespFail(e.getMessage());
            }
        }
    }

    protected <ReqData, P extends PreHandlerListener<ReqData> & ExceptHandlerListener, Resp extends RespResult<?>> void handler(
            @Nonnull final MonoSink<Resp> sink,
            @Nullable final ReqData reqData,
            @Nonnull final Resp resp,
            @Nonnull final P listener,
            @Nonnull final Consumer<Resp> handler
    ) {
        try {
            handler(reqData, resp, listener, handler);
        } finally {
            sink.success(resp);
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
        return Mono.create(sink -> handler(sink, null, new RespDataResult<Ret>(), listener,
                resp -> {
                    //查询数据
                    final List<Item> items = listener.query();
                    //结果处理
                    resp.setData(DataResult.<Ret>builder()
                            //数据总数
                            .total(items == null ? 0L : (long) items.size())
                            //数据集合转换
                            .rows(items == null || items.size() == 0 ? Lists.newLinkedList() :
                                    items.stream()
                                            .filter(Objects::nonNull)
                                            .map(listener)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList())
                            )
                            .build()
                    );
                })
        );
    }

    /**
     * 查询数据
     *
     * @param reqQuery 分页查询条件
     * @param listener 查询处理器
     * @param <ReqQry> 请求查询条件类型
     * @param <Qry>    转换后查询条件
     * @param <Item>   查询数据类型
     * @param <Ret>    结果数据类型
     * @return 查询结果
     */
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> Mono<RespDataResult<Ret>> buildQuery(
            @Nonnull final PagingQuery<ReqQry> reqQuery,
            @Nonnull final PagingQueryListener<ReqQry, Qry, Item, Ret> listener
    ) {
        return Mono.create(sink -> handler(sink, reqQuery.getQuery(), new RespDataResult<Ret>(), listener, resp -> buildQueryHandler(resp, reqQuery, listener)));
    }

    private <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> void buildQueryHandler(
            @Nonnull final RespDataResult<Ret> resp,
            @Nonnull final PagingQuery<ReqQry> reqQuery,
            @Nonnull final PagingQueryListener<ReqQry, Qry, Item, Ret> listener
    ) {
        //查询条件处理
        final PagingQuery<Qry> query = new PagingQuery<Qry>() {
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
        };
        //查询数据处理
        final PagingResult<Item> queryResult = listener.query(query);
        if (queryResult == null) {
            resp.setData(DataResult.<Ret>builder()
                    .total(0L)
                    .rows(Lists.newLinkedList())
                    .build()
            );
            return;
        }
        final Long totals = queryResult.getTotal();
        final List<Item> items = queryResult.getRows();
        resp.setData(DataResult.<Ret>builder()
                //总数据
                .total(totals == null ? 0L : totals)
                //数据处理
                .rows(items == null || items.size() == 0 ? Lists.newLinkedList() :
                        items.stream()
                                .filter(Objects::nonNull)
                                .map(listener)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .build()
        );
    }

    /**
     * 业务处理
     *
     * @param listener 处理器
     * @param <R>      返回数据类型
     * @return 处理结果
     */
    protected <R extends Serializable> Mono<RespResult<R>> action(@Nonnull final ProccessListener<Void, R> listener) {
        return Mono.create(sink -> handler(sink, null, new RespResult<R>().buildRespSuccess(null), listener,
                resp -> {
                    //业务处理
                    final R data = listener.apply(null);
                    if (data != null) {
                        resp.setData(data);
                    }
                }
        ));
    }

    /**
     * 业务处理
     *
     * @param req      请求数据
     * @param listener 处理器
     * @param <T>      请求数据类型
     * @param <R>      响应数据类型
     * @return 处理结果
     */
    protected <T extends Serializable, R extends Serializable> Mono<RespResult<R>> action(@Nonnull final Mono<T> req, @Nonnull final ProccessListener<T, R> listener) {
        final Function<Throwable, String> expHandler = e -> {
            if (e instanceof BindingResult) {
                final String error = ((BindingResult) e).getFieldErrors().stream()
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
            return e.getMessage();
        };
        return Mono.create(sink ->
                req.doOnError(Throwable.class, e -> sink.success(RespResult.buildFail(expHandler.apply(e))))
                        .doOnNext(data -> handler(
                                sink, data, RespResult.buildSuccess(null), listener,
                                resp -> {
                                    //业务处理
                                    final R ret = listener.apply(data);
                                    if (ret != null) {
                                        resp.setData(ret);
                                    }
                                }
                        ))
                        .subscribe()
        );
    }

    @Data
    protected static class ReqPagingQuery<T extends Serializable> implements PagingQuery<T> {
        /**
         * 页码
         */
        private Integer index;
        /**
         * 当前页数量
         */
        private Integer rows;
        /**
         * 查询条件
         */
        private T query;
    }
}
