package top.zenyoung.boot.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import top.zenyoung.boot.constant.AppConstants;
import top.zenyoung.boot.util.HttpUtils;
import top.zenyoung.common.util.JsonUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 请求日志打印-切面
 *
 * @author young
 */
@Slf4j
@Aspect
@RequiredArgsConstructor(staticName = "of")
public class RequestLogAspect extends BaseAspect implements AppConstants {
    private static final String NEW_LINE = "\n";
    private static final String SPACE_LINE = Strings.repeat("-", 50) + NEW_LINE;

    private final ObjectMapper objMapper;

    private static final String POINT_CUT = "(within(top.zenyoung.boot.controller.ErrorController) || " +
            "within(top.zenyoung.boot.controller.BaseController+)) && " +
            "@target(org.springframework.web.bind.annotation.RestController)";

    @Around(POINT_CUT)
    public Object logAround(@Nonnull final ProceedingJoinPoint joinPoint) throws Throwable {
        final long start = System.currentTimeMillis();
        final Object result = joinPoint.proceed();
        if (result instanceof Mono) {
            final AtomicReference<String> refTraceId = new AtomicReference<>(null);
            return HttpUtils.getRequest()
                    .flatMap(req -> ((Mono<?>) result).flatMap(ret -> Mono.deferContextual(ctx -> {
                        refTraceId.set(ctx.getOrDefault(TRACE_ID, ""));
                        return Mono.justOrEmpty(ret);
                    })).doOnSuccess(o -> {
                        final String traceId = refTraceId.get();
                        final List<String> logs = Lists.newLinkedList();
                        logs.add(SPACE_LINE);
                        if (Objects.nonNull(req)) {
                            logs.add("[" + traceId + "]请求ID:" + req.getId());
                            logs.add("[" + traceId + "]请求地址: " + req.getURI());
                            logs.add("[" + traceId + "]请求方式: " + req.getMethod());
                        }
                        final Signature signature;
                        if (Objects.nonNull(signature = joinPoint.getSignature())) {
                            logs.add(String.format("[" + traceId + "]请求处理: %s.%s", signature.getDeclaringTypeName(), signature.getName()));
                        }
                        final List<String> args = getReqParams(joinPoint);
                        if (!CollectionUtils.isEmpty(args)) {
                            logs.add("[" + traceId + "]请求参数: " + Joiner.on(",").skipNulls().join(args));
                        }
                        //检查响应数据
                        if (Objects.nonNull(o)) {
                            final String resJson = JsonUtils.toJson(objMapper, o);
                            logs.add("[" + traceId + "]响应数据: " + StringUtils.truncate(resJson));
                        }
                        if (start > 0) {
                            logs.add("[" + traceId + "]执行耗时:" + (System.currentTimeMillis() - start) + "ms");
                        }
                        logs.add(SPACE_LINE);
                        //打印日志处理
                        log.info(Joiner.on(NEW_LINE).skipNulls().join(logs));
                    }));
        }
        return result;
    }

    private List<String> getReqParams(@Nonnull final JoinPoint joinPoint) {
        return getReqArgs(joinPoint, arg -> {
            if (isPrimitive(arg.getClass())) {
                return arg.toString();
            }
            return JsonUtils.toJson(objMapper, arg);
        });
    }
}
