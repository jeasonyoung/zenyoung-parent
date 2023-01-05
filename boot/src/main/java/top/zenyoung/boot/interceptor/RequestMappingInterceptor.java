package top.zenyoung.boot.interceptor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import top.zenyoung.boot.util.HttpUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 拦截器-接口
 *
 * @author young
 */
public interface RequestMappingInterceptor extends HandlerInterceptor {

    /**
     * 获取排序号
     *
     * @return 排序号
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 获取执行模型
     *
     * @return 执行模型集合
     */
    default List<String> getIncludePatterns() {
        return null;
    }

    /**
     * 获取非执行模型
     *
     * @return 非执行模型集合
     */
    default List<String> getExcludePatterns() {
        return null;
    }

    /**
     * 是否支持报文类型
     *
     * @param contentType 报文类型
     * @return 是否支持
     */
    default boolean supportsContentType(final MediaType contentType) {
        return true;
    }

    /**
     * 前置业务处理器
     *
     * @param req     请求对象
     * @param res     响应对象
     * @param handler 拦截函数处理
     * @return 处理结果
     * @throws Exception 异常
     */
    @Override
    default boolean preHandle(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse res, @Nonnull final Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            final MediaType contentType = HttpUtils.getContentType(req);
            if (handlerMethod.hasMethodAnnotation(RequestMapping.class) && this.supportsContentType(contentType)) {
                //执行处理
                return this.handler(req, res, handlerMethod);
            }
        }
        return true;
    }

    /**
     * 业务处理器
     *
     * @param req     请求对象
     * @param res     响应对象
     * @param handler 拦截方法处理器
     * @return 处理结果
     */
    boolean handler(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse res, @Nonnull final HandlerMethod handler);
}
