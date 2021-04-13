package top.zenyoung.web.listener;

import top.zenyoung.common.model.UserPrincipal;

import java.util.function.BiFunction;

/**
 * 认证用户-业务处理监听器
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/8 2:30 下午
 **/
public interface AuthProccessListener<A extends UserPrincipal, T, R> extends BiFunction<A, T, R>, PreHandlerListener<T>, ExceptHandlerListener {

}
