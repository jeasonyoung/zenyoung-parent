package top.zenyoung.webflux;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.common.response.DataResult;
import top.zenyoung.common.response.RespDataResult;
import top.zenyoung.common.response.RespResult;
import top.zenyoung.common.response.ResultCode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
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
 * @date 2019/10/21 9:13 下午
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
        return new ArrayList<>() {
            {
                //数据操作失败
                add(new ExceptHandler(502, java.sql.SQLIntegrityConstraintViolationException.class));
            }
        };
    }

    private <ReqData, P extends PreHandlerListener<ReqData> & ExceptionHandlerListener, Resp extends RespResult<?>> void handler(
            @Nonnull final MonoSink<Resp> sink,
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
        } catch (Throwable ex) {
            if (handlerNotException(resp, ex, listener)) {
                resp.buildResult(ex);
            }
            log.error("handler-exp: {}\n {}", ex.getMessage(), ex.getStackTrace());
        }
        sink.success(resp);
    }

    /**
     * 查询数据处理
     *
     * @param listener 处理器
     * @param <Item>   查询数据类型
     * @param <Ret>    结果数据类型
     * @return 查询结果
     */
    protected <Item extends Serializable, Ret extends Serializable> Mono<RespDataResult<Ret>> buildQuery(@Nonnull final QueryListener<Item, Ret> listener) {
        return Mono.create(sink -> handler(sink, null, new RespDataResult<Ret>(), listener, resp -> {
                    //查询数据
                    final List<Item> items = listener.query();
                    //结果处理
                    resp.setData(DataResult.<Ret>builder()
                            //数据总数
                            .totals(CollectionUtils.isEmpty(items) ? 0L : (long) items.size())
                            //数据集合转换
                            .rows(CollectionUtils.isEmpty(items) ? Lists.newLinkedList() : items.stream()
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
     * 分页查询处理
     *
     * @param listener 分页处理器
     * @param <ReqQry> 请求条件类型
     * @param <Qry>    查询条件类型
     * @param <Item>   查询数据类型
     * @param <Ret>    查询结果类型
     * @return 分页查询结果
     */
    protected <ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> Mono<RespDataResult<Ret>> buildQuery(
            @Nonnull final PagingQuery<ReqQry> reqQuery,
            @Nonnull final PagingQueryListener<ReqQry, Qry, Item, Ret> listener
    ) {
        return Mono.create(sink -> handler(sink, reqQuery.getQuery(), new RespDataResult<Ret>(), listener, resp -> {
                    //查询条件处理
                    final PagingQuery<Qry> query = new PagingQuery<>() {
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
                    if (queryResult != null) {
                        //总数据
                        final Long totals = queryResult.getTotals();
                        //数据集合
                        final List<Item> items = queryResult.getRows();
                        //结果数据处理
                        resp.setData(DataResult.<Ret>builder()
                                //总数据
                                .totals(totals == null ? 0L : totals)
                                //数据处理
                                .rows(CollectionUtils.isEmpty(items) ? Lists.newLinkedList() : items.stream()
                                        .filter(Objects::nonNull)
                                        .map(listener)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList())
                                )
                                .build()
                        );
                    }
                })
        );
    }

    /**
     * 业务处理
     *
     * @param listener 处理器
     * @param <R>      返回类型
     * @return 处理结果
     */
    protected <R extends Serializable> Mono<RespResult<R>> action(@Nonnull final ProccessListener<Void, R> listener) {
        return Mono.create(sink -> handler(sink, null, RespResult.<R>builder().build().buildResult(ResultCode.Success), listener, resp -> {
                    //业务处理
                    final R data = listener.apply(null);
                    if (data != null) {
                        resp.setData(data);
                    }
                })
        );
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
        return Mono.create(sink ->
                req.doOnError(Throwable.class, bindExp -> sink.success(RespResult.<R>builder().build().buildResult(bindExp)))
                        .doOnNext(data -> handler(sink, data, RespResult.<R>builder().build().buildResult(ResultCode.Success), listener, resp -> {
                                    //业务处理
                                    final R ret = listener.apply(data);
                                    if (ret != null) {
                                        resp.setData(ret);
                                    }
                                })
                        )
                        .subscribe()
        );
    }

    private boolean handlerNotException(@Nonnull final RespResult<?> respResult, @Nullable final Throwable ex, @Nonnull final ExceptionHandlerListener listener) {
        log.debug("handlerNotException(respResult: {},ex: {},listener: {})", respResult, ex, listener);
        //获取异常处理器集合
        final List<ExceptHandler> exceptHandlers = getExceptHandlers();
        //添加异常处理集合
        listener.getExceptionHandlers(exceptHandlers);
        if (ex != null && !CollectionUtils.isEmpty(exceptHandlers)) {
            final Map<Class<? extends Throwable>, ExceptHandler> handlerMap = exceptHandlers.stream()
                    .collect(Collectors.toMap(ExceptHandler::getEClass, item -> item, (n, o) -> n));
            return handlerNotExceptionCause(respResult, ex, handlerMap);
        }
        return true;
    }

    private boolean handlerNotExceptionCause(@Nonnull final RespResult<?> respResult, @Nonnull final Throwable ex, @Nonnull final Map<Class<? extends Throwable>, ExceptHandler> handlerMap) {
        final Class<?> cls = ex.getClass();
        if (cls != null) {
            final ExceptHandler handler = handlerMap.getOrDefault(cls, null);
            if (handler != null) {
                respResult.buildResult(handler.code, ex.getMessage());
                return false;
            }
            final Throwable cause = ex.getCause();
            if (cause != null) {
                return handlerNotExceptionCause(respResult, cause, handlerMap);
            }
        }
        return true;
    }

    /**
     * 业务处理监听器
     *
     * @param <T> 入参类型
     * @param <R> 出参类型
     */
    protected interface ProccessListener<T, R> extends Function<T, R>, PreHandlerListener<T>, ExceptionHandlerListener {

    }

    /**
     * 查询处理监听器
     *
     * @param <Item> 查询数据类型
     * @param <Ret>  结果数据类型
     */
    protected interface QueryListener<Item extends Serializable, Ret extends Serializable> extends Function<Item, Ret>, PreHandlerListener<Void>, ExceptionHandlerListener {

        /**
         * 查询数据
         *
         * @return 查询结果集合
         */
        List<Item> query();
    }

    /**
     * 分页查询处理监听器
     *
     * @param <ReqQry> 请求查询类型
     * @param <Qry>    查询类型
     * @param <Item>   查询数据类型
     * @param <Ret>    查询结果类型
     */
    protected interface PagingQueryListener<ReqQry extends Serializable, Qry extends Serializable, Item extends Serializable, Ret extends Serializable> extends Function<Item, Ret>, PreHandlerListener<ReqQry>, ExceptionHandlerListener {

        /**
         * 查询条件转换
         *
         * @param reqQry 请求查询条件
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

    /**
     * 前置业务处理监听器
     */
    protected interface PreHandlerListener<T> {
        /**
         * 前置业务处理
         *
         * @param reqData 请求数据
         */
        default void preHandler(@Nullable final T reqData) {

        }
    }

    /**
     * 异常处理接口
     */
    protected interface ExceptionHandlerListener {

        /**
         * 获取异常处理集合
         *
         * @param exceptHandlers 异常处理集合
         */
        default void getExceptionHandlers(@Nonnull final List<ExceptHandler> exceptHandlers) {

        }
    }

    /**
     * 异常处理
     */
    @Data
    @AllArgsConstructor
    protected static class ExceptHandler {
        /**
         * 响应代码
         */
        private int code;
        /**
         * 错误类型
         */
        private Class<? extends Throwable> eClass;
    }

    /**
     * 查询条件
     *
     * @param <T> 查询条件类型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    protected static class DataPagingQuery<T extends Serializable> implements PagingQuery<T>, Serializable {
        /**
         * 页码
         */
        private Integer index;
        /**
         * 每页数据量
         */
        private Integer rows;
        /**
         * 查询条件
         */
        private T query;
    }
}