package top.zenyoung.web.controller.interceptor;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import top.zenyoung.common.event.OperateLogEvent;
import top.zenyoung.web.OperateLog;
import top.zenyoung.web.controller.util.HttpUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 操作日志拦截器
 *
 * @author young
 */
@Slf4j
public class OperateLogInterceptor implements HandlerInterceptor {
    private final ApplicationContext context;

    /**
     * 构造函数
     *
     * @param context Spring上下文
     */
    public OperateLogInterceptor(@Nonnull final ApplicationContext context) {
        this.context = context;
    }

    @Override
    public boolean preHandle(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response, @Nonnull final Object handler) {
        if (handler instanceof HandlerMethod) {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            //获取当前请求方法
            final OperateLog operateLog = handlerMethod.getMethodAnnotation(OperateLog.class);
            if (operateLog != null && !Strings.isNullOrEmpty(operateLog.value())) {
                final String methodName = handlerMethod.getBeanType().getName() + "." + handlerMethod.getMethod().getName();
                final List<String> params = Stream.of(handlerMethod.getMethodParameters())
                        .map(MethodParameter::getParameterName)
                        .collect(Collectors.toList());
                final OperateLogEvent event = OperateLogEvent.of(
                        request.getUserPrincipal(),
                        operateLog.value(),
                        request.getRequestURI(),
                        methodName,
                        params,
                        request.getHeader("User-Agent"),
                        HttpUtils.getClientIpAddr(request)
                );
                log.debug("preHandle-event: {}", event);
                context.publishEvent(event);
            }
        }
        return true;
    }
}
