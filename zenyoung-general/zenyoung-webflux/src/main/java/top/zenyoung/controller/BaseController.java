package top.zenyoung.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import top.zenyoung.common.model.*;
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
        return Mono.create(sink -> handler(sink, reqQuery.getQuery(), new RespDataResult<Ret>(), listener,
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
                        resp.setData(DataResult.<Ret>builder()
                                .total(0L)
                                .rows(Lists.newLinkedList())
                                .build()
                        );
                        return;
                    }
                    //查询结果处理
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
        ));
    }

    /**
     * 业务处理-无入参验证
     *
     * @param resp     响应对象
     * @param listener 业务处理器
     * @param <R>      返回数据类型
     * @param <Resp>   响应数据类型
     * @return 响应数据
     */
    protected <R extends Serializable, Resp extends RespResult<R>> Mono<Resp> action(@Nonnull final Resp resp, @Nonnull final ProccessListener<Void, R> listener) {
        return Mono.create(sink -> handler(sink, null, resp, listener,
                respRet -> {
                    final R data = listener.apply(null);
                    if (data != null) {
                        respRet.setData(data);
                    }
                }
        ));
    }

    /**
     * 业务处理-无入参验证
     *
     * @param listener 处理器
     * @param <R>      返回数据类型
     * @return 处理结果
     */
    protected <R extends Serializable> Mono<RespResult<R>> action(@Nonnull final ProccessListener<Void, R> listener) {
        return action(new RespResult<R>().buildRespSuccess(null), listener);
    }

    /**
     * 业务处理-删除处理
     *
     * @param process 删除处理器
     * @return 处理结果
     */
    protected Mono<RespDeleteResult> actionDelete(@Nonnull final ProccessDeleteListener process) {
        return action(new RespDeleteResult().buildSuccess(),
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

    /**
     * 业务异常消息处理
     *
     * @param throwable 异常
     * @return 异常消息
     */
    protected String actionExceptionHandler(@Nullable final Throwable throwable) {
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
     * 业务处理-有入参验证
     *
     * @param req                入参数据
     * @param respSuccessHandler 响应成功数据
     * @param respFailHandler    响应失败处理
     * @param listener           业务处理器
     * @param <T>                入参数据类型
     * @param <R>                出参数据类型
     * @param <Resp>             响应数据
     * @return 处理结果
     */
    protected <T extends Serializable, R extends Serializable, Resp extends RespResult<R>> Mono<Resp> action(
            @Nonnull final Mono<T> req,
            @Nonnull final Supplier<Resp> respSuccessHandler,
            @Nonnull final Function<String, Resp> respFailHandler,
            @Nonnull final ProccessListener<T, R> listener) {
        return Mono.create(sink -> req.doOnError(Throwable.class, e -> sink.success(respFailHandler.apply(actionExceptionHandler(e))))
                .doOnNext(data -> handler(sink, data, respSuccessHandler.get(), listener,
                        respRet -> {
                            //业务处理
                            final R ret = listener.apply(data);
                            if (ret != null) {
                                respRet.setData(ret);
                            }
                        }
                )).subscribe()
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
        return action(req, () -> RespResult.buildSuccess(null), RespResult::buildFail, listener);
    }


    /**
     * 业务处理-新增
     *
     * @param req     请求数据
     * @param process 处理器
     * @param <T>     请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> Mono<RespAddResult> actionAdd(@Nonnull final Mono<T> req, @Nonnull final ProccessListener<T, String> process) {
        return action(req, () -> RespAddResult.buildSuccess(null), RespAddResult::buildFail,
                new ProccessListener<T, AddResult>() {
                    @Override
                    public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                        process.getExceptHandlers(handlers);
                    }

                    @Override
                    public void preHandler(@Nullable final T reqData) {
                        process.preHandler(reqData);
                    }

                    @Override
                    public AddResult apply(final T data) {
                        return new AddResult(process.apply(data));
                    }
                }
        );
    }

    /**
     * 业务处理-修改
     *
     * @param req      请求数据
     * @param listener 处理器
     * @param <T>      请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> Mono<RespModifyResult> actionModify(@Nonnull final Mono<T> req, @Nonnull final ProccessModifyListener<T> listener) {
        return action(req, () -> new RespModifyResult().buildSuccess(), RespModifyResult::buildFail,
                new ProccessListener<T, Serializable>() {

                    @Override
                    public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                        listener.getExceptHandlers(handlers);
                    }

                    @Override
                    public void preHandler(@Nullable final T reqData) {
                        listener.preHandler(reqData);
                    }

                    @Override
                    public Serializable apply(final T data) {
                        listener.accept(data);
                        return null;
                    }
                }
        );
    }

    protected interface ProccessModifyListener<T> extends Consumer<T>, PreHandlerListener<T>, ExceptHandlerListener {

    }

    protected interface ProccessDeleteListener extends ProccessModifyListener<Void> {

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