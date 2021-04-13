package top.zenyoung.web.listener;

import java.util.function.Consumer;

/**
 * 修改处理-监听器接口
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/9 10:59 下午
 **/
public interface ProccessModifyListener<T> extends Consumer<T>, PreHandlerListener<T>, ExceptHandlerListener {
    
}
