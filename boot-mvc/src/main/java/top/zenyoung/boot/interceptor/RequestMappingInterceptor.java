package top.zenyoung.boot.interceptor;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import top.zenyoung.boot.util.HttpUtils;

import javax.annotation.Nonnull;
import java.util.Collection;

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
    default Collection<String> getIncludePatterns() {
        return Lists.newArrayList();
    }

    /**
     * 获取非执行模型
     *
     * @return 非执行模型集合
     */
    default Collection<String> getExcludePatterns() {
        return Lists.newArrayList();
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
     */
    @Override
    default boolean preHandle(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse res, @Nonnull final Object handler) {
        if (handler instanceof HandlerMethod method) {
            final MediaType contentType = HttpUtils.getContentType(req);
            if (method.hasMethodAnnotation(RequestMapping.class) && this.supportsContentType(contentType)) {
                //执行处理
                return this.handler(req, res, method);
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
