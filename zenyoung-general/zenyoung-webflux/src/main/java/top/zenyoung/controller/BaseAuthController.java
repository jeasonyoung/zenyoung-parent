package top.zenyoung.controller;

import reactor.core.publisher.Mono;
import top.zenyoung.common.model.*;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.controller.listener.AuthProccessListener;
import top.zenyoung.controller.listener.ExceptHandlerListener;
import top.zenyoung.controller.listener.PreHandlerListener;
import top.zenyoung.controller.listener.ProccessListener;
import top.zenyoung.controller.model.ExceptHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 认证用户-控制器-基类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/8 2:32 下午
 **/
public abstract class BaseAuthController<A extends UserPrincipal> extends BaseController {

    /**
     * 认证用户数据转换
     *
     * @param principal 认证用户
     * @return 转换数据
     */
    @Nonnull
    protected abstract A convert(@Nonnull final UserPrincipal principal);

    /**
     * 检查认证用户
     *
     * @param principal 认证用户
     */
    protected abstract void checkAuth(@Nonnull final A principal);

    /**
     * 查询数据
     *
     * @param principal      认证用户
     * @param queryHandler   查询处理器
     * @param convertHandler 查询结果转换处理器
     * @param <Item>         查询数据类型
     * @param <Ret>          结果数据类型
     * @return 查询结果
     */
    protected <Item extends Serializable, Ret extends Serializable> Mono<RespDataResult<Ret>> buildQuery(
            @Nonnull final UserPrincipal principal,
            @Nonnull final Function<A, List<Item>> queryHandler,
            @Nonnull final Function<Item, Ret> convertHandler
    ) {
        return buildQuery(() -> {
            final A auth = convert(principal);
            checkAuth(auth);
            return queryHandler.apply(auth);
        }, convertHandler);
    }

    /**
     * 分页查询数据
     *
     * @param principal            认证用户
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
            @Nonnull final UserPrincipal principal,
            @Nonnull final PagingQuery<ReqQry> reqQuery,
            @Nonnull final BiFunction<A, ReqQry, Qry> queryConvertHandler,
            @Nonnull final Function<PagingQuery<Qry>, PagingResult<Item>> pagingQueryHandler,
            @Nonnull final Function<Item, Ret> resultConvertHandler
    ) {
        return buildQuery(reqQuery, reqQry -> {
            final A auth = convert(principal);
            checkAuth(auth);
            return queryConvertHandler.apply(auth, reqQry);
        }, pagingQueryHandler, resultConvertHandler);
    }

    /**
     * 业务处理-无入参验证
     *
     * @param principal 认证用户
     * @param process   处理器
     * @param <R>       返回数据类型
     * @return 处理结果
     */
    protected <R extends Serializable> Mono<RespResult<R>> action(
            @Nonnull final UserPrincipal principal,
            @Nonnull final AuthProccessListener<A, Void, R> process
    ) {
        return action(new ProccessListener<Void, R>() {

            @Override
            public R apply(final Void aVoid) {
                final A auth = convert(principal);
                checkAuth(auth);
                return process.apply(auth, aVoid);
            }

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                process.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final Void reqData) {
                process.preHandler(reqData);
            }
        });
    }

    /**
     * 业务处理
     *
     * @param principal 认证用户
     * @param req       请求数据
     * @param process   处理器
     * @param <T>       请求数据类型
     * @param <R>       响应数据类型
     * @return 处理结果
     */
    protected <T extends Serializable, R extends Serializable> Mono<RespResult<R>> action(
            @Nonnull final UserPrincipal principal,
            @Nonnull final Mono<T> req,
            @Nonnull final AuthProccessListener<A, T, R> process) {
        return action(req, new ProccessListener<T, R>() {

            @Override
            public R apply(final T t) {
                final A auth = convert(principal);
                checkAuth(auth);
                return process.apply(auth, t);
            }

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                process.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final T reqData) {
                process.preHandler(reqData);
            }
        });
    }

    /**
     * 业务处理-新增
     *
     * @param principal 认证用户
     * @param req       请求数据
     * @param process   处理器
     * @param <T>       请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> Mono<RespAddResult> actionAdd(
            @Nonnull final UserPrincipal principal,
            @Nonnull final Mono<T> req,
            @Nonnull final AuthProccessListener<A, T, String> process) {
        return actionAdd(req, new ProccessListener<T, String>() {

            @Override
            public String apply(final T t) {
                final A auth = convert(principal);
                checkAuth(auth);
                return process.apply(auth, t);
            }

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                process.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final T reqData) {
                process.preHandler(reqData);
            }
        });
    }

    /**
     * 业务处理-修改
     *
     * @param principal 认证用户
     * @param req       请求数据
     * @param process   处理器
     * @param <T>       请求数据类型
     * @return 处理结果
     */
    protected <T extends Serializable> Mono<RespModifyResult> actionModify(
            @Nonnull final UserPrincipal principal,
            @Nonnull final Mono<T> req,
            @Nonnull final AuthProccessModifyListener<A, T> process) {
        return actionModify(req, new ProccessModifyListener<T>() {

            @Override
            public void accept(final T t) {
                final A auth = convert(principal);
                checkAuth(auth);
                process.accept(auth, t);
            }

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                process.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final T reqData) {
                process.preHandler(reqData);
            }
        });
    }

    /**
     * 业务处理-删除处理
     *
     * @param principal 认证用户
     * @param process   删除处理器
     * @return 处理结果
     */
    protected Mono<RespDeleteResult> actionDelete(
            @Nonnull final UserPrincipal principal,
            @Nonnull final AuthProccessDeleteListener<A> process
    ) {
        return actionDelete(new ProccessDeleteListener() {

            @Override
            public void accept(final Void aVoid) {
                final A auth = convert(principal);
                checkAuth(auth);
                process.accept(auth, aVoid);
            }

            @Override
            public void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {
                process.getExceptHandlers(handlers);
            }

            @Override
            public void preHandler(@Nullable final Void reqData) {
                process.preHandler(reqData);
            }
        });
    }

    protected interface AuthProccessModifyListener<A, T> extends BiConsumer<A, T>, PreHandlerListener<T>, ExceptHandlerListener {

    }

    protected interface AuthProccessDeleteListener<A> extends AuthProccessModifyListener<A, Void> {

    }
}
