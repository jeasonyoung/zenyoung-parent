package top.zenyoung.web.listener;

import java.util.function.BiConsumer;

/**
 * 认证用户-修改处理-监听器接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/8/9 11:03 下午
 **/
public interface AuthProccessModifyListener<A, T> extends BiConsumer<A, T>, PreHandlerListener<T>, ExceptHandlerListener {

}
