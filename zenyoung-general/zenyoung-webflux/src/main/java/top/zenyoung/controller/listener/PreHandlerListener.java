package top.zenyoung.controller.listener;

import javax.annotation.Nullable;

/**
 * 前置业务处理监听器
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/7 12:43 下午
 **/
public interface PreHandlerListener<T> {

    /**
     * 前置业务处理
     *
     * @param reqData 请求数据
     */
    default void preHandler(@Nullable final T reqData) {

    }
}
