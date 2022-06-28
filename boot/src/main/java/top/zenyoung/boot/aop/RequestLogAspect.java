package top.zenyoung.boot.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
@Component
@RequiredArgsConstructor
public class RequestLogAspect extends BaseAspect {
    private static final ThreadLocal<Long> LOCAL = ThreadLocal.withInitial(() -> 0L);
    private static final ThreadLocal<List<String>> LOG = ThreadLocal.withInitial(Lists::newLinkedList);
    private static final String SPACE_LINE = Strings.repeat("-", 50);

    private final ObjectMapper objMapper;

    private static final String ALL_POINT_CUT = "@annotation(org.springframework.web.bind.annotation.RequestMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)";

    @Before(ALL_POINT_CUT)
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

    @AfterReturning(pointcut = ALL_POINT_CUT, returning = "jsonResult")
    public void doAfterReturning(final JoinPoint joinPoint, final Object jsonResult) {
        final List<String> prev = LOG.get();
        final List<String> logs = prev == null ? Lists.newLinkedList() : prev;
        //检查响应数据
        if (jsonResult != null) {
            logs.add("响应数据: " + JsonUtils.toJson(objMapper, jsonResult));
        }
        printLogsHandler(logs);
    }

    @AfterThrowing(pointcut = ALL_POINT_CUT, throwing = "e")
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
        log.info(Joiner.on("\n").skipNulls().join(logs));
        log.info("\n");
    }

    private List<String> getReqParams(final JoinPoint joinPoint) {
        return getReqArgs(joinPoint, arg -> {
            if (isPrimitive(arg.getClass())) {
                return arg + "";
            }
            return JsonUtils.toJson(objMapper, arg);
        });
    }

}
