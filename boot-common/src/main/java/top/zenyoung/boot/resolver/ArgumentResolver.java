package top.zenyoung.boot.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 参数分解器接口
 *
 * @author young
 */
public interface ArgumentResolver extends HandlerMethodArgumentResolver {

    /**
     * 分解参数处理
     *
     * @param parameter     the method parameter to resolve. This parameter must
     *                      have previously been passed to {@link #supportsParameter} which must
     *                      have returned {@code true}.
     * @param mavContainer  the ModelAndViewContainer for the current request
     * @param webRequest    the current request
     * @param binderFactory a factory for creating {@link WebDataBinderFactory} instances
     * @return 参数数据
     */
    @Override
    default Object resolveArgument(@Nonnull final MethodParameter parameter,
                                   @Nullable final ModelAndViewContainer mavContainer,
                                   @Nonnull final NativeWebRequest webRequest,
                                   @Nullable final WebDataBinderFactory binderFactory) {
        return resolveArgument(parameter, webRequest);
    }

    /**
     * 分解参数处理
     *
     * @param parameter MethodParameter
     * @param req       HttpServletRequest
     * @return 参数数据
     */
    Object resolveArgument(@Nonnull final MethodParameter parameter, @Nonnull final WebRequest req);
}
