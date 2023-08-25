package top.zenyoung.boot.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.zenyoung.boot.util.HttpUtils;
import top.zenyoung.common.util.JsonUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 请求日志打印-切面
 *
 * @author young
 */
@Slf4j
@Aspect
@RequiredArgsConstructor(staticName = "of")
public class RequestLogAspect extends BaseAspect implements DisposableBean {
    private static final ThreadLocal<Long> LOCAL = ThreadLocal.withInitial(() -> 0L);
    private static final ThreadLocal<List<String>> LOG = ThreadLocal.withInitial(Lists::newLinkedList);

    private static final String NEW_LINE = "\n";
    private static final String SPACE_LINE = Strings.repeat("-", 50) + NEW_LINE;

    private final ObjectMapper objMapper;

    private static final String POINT_CUT = "(within(top.zenyoung.boot.controller.ErrorController) || " +
            "within(top.zenyoung.boot.controller.BaseController+)) && " +
            "@target(org.springframework.web.bind.annotation.RestController)";

    @Pointcut(POINT_CUT)
    public void logPointcut() {
        log.info("日志 AOP: {}", POINT_CUT);
    }

    @Before("logPointcut()")
    public void doBefore(final JoinPoint joinPoint) {
        LOCAL.set(System.currentTimeMillis());
        final List<String> logs = Lists.newLinkedList();
        logs.add(SPACE_LINE);
        final HttpServletRequest request = HttpUtils.getWebRequest();
        if (request != null) {
            logs.add("请求地址: " + request.getRequestURI());
            logs.add("请求方式: " + request.getMethod());
        }
        final Signature signature = joinPoint.getSignature();
        if (signature != null) {
            logs.add(String.format("请求处理: %s.%s", signature.getDeclaringTypeName(), signature.getName()));
        }
        final List<String> args = getReqParams(joinPoint);
        if (!CollectionUtils.isEmpty(args)) {
            logs.add("请求参数: " + Joiner.on(",").skipNulls().join(args));
        }
        //请求参数
        LOG.set(logs);
    }

    @AfterReturning(pointcut = "logPointcut()", returning = "jsonResult")
    public void doAfterReturning(final JoinPoint joinPoint, final Object jsonResult) {
        final List<String> prev = LOG.get();
        final List<String> logs = prev == null ? Lists.newLinkedList() : prev;
        //检查响应数据
        if (jsonResult != null) {
            final String json = JsonUtils.toJson(objMapper, jsonResult);
            logs.add("响应数据: " + StringUtils.truncate(json));
        }
        printLogsHandler(logs);
    }

    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void doAfterThrowing(final JoinPoint joinPoint, final Exception e) {
        final List<String> prev = LOG.get();
        final List<String> logs = prev == null ? Lists.newLinkedList() : prev;
        //异常处理
        if (e != null) {
            logs.add("发生异常: " + e.getMessage());
        }
        printLogsHandler(logs);
    }

    private void printLogsHandler(@Nonnull final List<String> logs) {
        //执行耗时
        final Long start = LOCAL.get();
        if (start != null && start > 0) {
            logs.add("执行耗时:" + (System.currentTimeMillis() - start) + "ms");
        }
        logs.add(SPACE_LINE);
        //打印日志处理
        log.info(Joiner.on(NEW_LINE).skipNulls().join(logs));
    }

    private List<String> getReqParams(final JoinPoint joinPoint) {
        return getReqArgs(joinPoint, arg -> {
            if (isPrimitive(arg.getClass())) {
                return arg.toString();
            }
            return JsonUtils.toJson(objMapper, arg);
        });
    }

    @Override
    public void destroy() throws Exception {
        LOCAL.remove();
        LOG.remove();
    }
}
