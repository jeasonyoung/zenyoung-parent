package top.zenyoung.controller.listener;

import java.util.function.Function;

/**
 * 业务处理监听器
 *
 * @author yangyong
 * @version 1.0
 *  2020/2/7 12:47 下午
 **/
public interface ProccessListener<T, R> extends Function<T, R>, PreHandlerListener<T>, ExceptHandlerListener {
    
}
