package top.zenyoung.web.listener;

import top.zenyoung.web.ExceptHandler;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 异常处理接口
 *
 * @author yangyong
 * @version 1.0
 *  2020/2/7 12:31 下午
 **/
public interface ExceptHandlerListener {

    /**
     * 获取异常处理集合
     *
     * @param handlers 异常处理集合
     */
    default void getExceptHandlers(@Nonnull final List<ExceptHandler> handlers) {

    }
}
