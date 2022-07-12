package top.zenyoung.boot.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import top.zenyoung.boot.annotation.OperaLog;
import top.zenyoung.boot.dto.OperaLogDTO;
import top.zenyoung.boot.model.OperaType;
import top.zenyoung.boot.util.HttpUtils;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.util.ClassUtils;
import top.zenyoung.common.util.JsonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 操作日志记录切面处理器
 *
 * @author young
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperaLogAspect extends BaseAspect {
    private static final ThreadLocal<Long> LOCAL_CACHE = ThreadLocal.withInitial(() -> 0L);

    private final ObjectMapper objMapper;
    private final ApplicationContext context;

    private String getFullMethod(@Nonnull final JoinPoint joinPoint) {
        final String className = joinPoint.getTarget().getClass().getName();
        final String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }


    private Long getTakeUpTime() {
        final Long start = LOCAL_CACHE.get();
        if (start != null && start > 0) {
            return System.currentTimeMillis() - start;
        }
        return null;
    }

    @Before("@annotation(controllerLog)")
    public void doBefore(final JoinPoint joinPoint, final OperaLog controllerLog) {
        if (log.isDebugEnabled()) {
            log.debug("doBefore(joinPoint: {},controllerLog: {})...", joinPoint, controllerLog);
        }
        LOCAL_CACHE.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(final JoinPoint joinPoint, final OperaLog controllerLog, final Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(final JoinPoint joinPoint, final OperaLog controllerLog, final Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, final OperaLog controllerLog, final Exception e, final Object jsonResult) {
        if (log.isDebugEnabled()) {
            log.debug("handleLog(joinPoint: {},controllerLog: {},e: {},jsonResult: {})...", joinPoint, controllerLog, e, jsonResult);
        }
        if (joinPoint != null && controllerLog != null) {
            try {
                //执行时间
                final Long takeUpTime = getTakeUpTime();
                //日志数据
                final OperaLogDTO operLog = OperaLogDTO.builder().status(Status.Enable).takeUpTime(takeUpTime).createTime(new Date()).build();
                //执行方法
                operLog.setMethod(getFullMethod(joinPoint) + "()");
                try {
                    //操作用户
                    SecurityUtils.getUserOpt().ifPresent(u -> {
                        operLog.setOperaUserId(u.getId());
                        operLog.setOperaUserName(u.getAccount());
                    });
                } catch (Throwable ex) {
                    log.warn("handleLog-exp: {}", ex.getMessage());
                }
                //获取IP地址
                operLog.setOperaUserIpAddr(HttpUtils.getClientIpAddr());
                HttpUtils.getWebRequestOpt().ifPresent(web -> {
                    //请求地址
                    operLog.setReqUrl(web.getRequestURI());
                    //请求方式
                    operLog.setReqMethod(web.getMethod());
                });
                //异常处理
                if (e != null) {
                    operLog.setStatus(Status.Disable);
                    Optional.ofNullable(e.getMessage())
                            .ifPresent(err -> {
                                if (!Strings.isNullOrEmpty(err)) {
                                    final int len = err.length(), max = 2000;
                                    operLog.setErrorMsg(len < max ? err : err.substring(0, max));
                                }
                            });
                }
                //按注解处理操作日志
                handleAnnoMethod(joinPoint, controllerLog, operLog, jsonResult);
                //保存数据处理
                context.publishEvent(operLog);
            } catch (Throwable ex) {
                log.error("handleLog(joinPoint: {},controllerLog: {},jsonResult: {})-exp: {}", joinPoint, controllerLog, jsonResult, ex.getMessage());
            }
        }
    }

    private void handleAnnoMethod(@Nonnull final JoinPoint joinPoint, @Nonnull final OperaLog log, @Nonnull final OperaLogDTO operLog, @Nullable final Object jsonResult) {
        //业务类型
        operLog.setOperaType(log.operaType());
        //业务模块
        operLog.setTitle(log.title());
        //是否需要保存请求参数
        if (log.isSaveReqData()) {
            buildReqParams(joinPoint, log, operLog);
        }
        //是否需要保存响应参数
        if (log.isSaveRespData() && jsonResult != null) {
            final String resJson = JsonUtils.toJson(objMapper, jsonResult);
            operLog.setRespResult(resJson);
            //主键处理
            if (!Strings.isNullOrEmpty(resJson) && log.operaType() == OperaType.Add && !Strings.isNullOrEmpty(log.primaryKey())) {
                final Map<String, Object> retMap = JsonUtils.fromJsonToMap(objMapper, resJson, Object.class);
                if (!CollectionUtils.isEmpty(retMap)) {
                    final Object val = recursionSearch(retMap, log.primaryKey());
                    if (val != null) {
                        operLog.setPrimaryKeyVal(getPrimaryVal(val));
                    }
                }
            }
        }
    }

    private void buildReqParams(@Nonnull final JoinPoint joinPoint, @Nonnull final OperaLog log, @Nonnull final OperaLogDTO operLog) {
        final Map<String, LogReqParamVal> reqParamMaps = Maps.newLinkedHashMap();
        final Object[] args = joinPoint.getArgs();
        final int len;
        if (args != null && (len = args.length) > 0) {
            final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            final Method method = signature.getMethod();
            final Map<String, String> implicitParamsMap = getImplicitParams(method);
            final Annotation[][] paramAnnos = method.getParameterAnnotations();
            final String[] argNames = signature.getParameterNames();
            String argName, argTitle;
            Object argVal;
            for (int i = 0; i < len; i++) {
                //参数值
                argVal = args[i];
                if (argVal != null && isNotFilterObject(argVal)) {
                    argName = i > argNames.length ? null : argNames[i];
                    if (!Strings.isNullOrEmpty(argName)) {
                        //参数中文名称
                        argTitle = CollectionUtils.isEmpty(implicitParamsMap) ? null : implicitParamsMap.getOrDefault(argName, null);
                        //参数注解
                        final Annotation[] annotations = i > paramAnnos.length ? null : paramAnnos[i];
                        //参数处理
                        buildParamArgValHandler(argName, argTitle, argVal, annotations, log, operLog, reqParamMaps);
                    }
                }
            }
        }
        operLog.setReqParams(JsonUtils.toJson(objMapper, reqParamMaps));
    }

    private static Map<String, String> getImplicitParams(@Nonnull final Method method) {
        if (method.isAnnotationPresent(ApiImplicitParams.class)) {
            final ApiImplicitParams paramsAnno = method.getAnnotation(ApiImplicitParams.class);
            final ApiImplicitParam[] pas;
            if ((pas = paramsAnno.value()) != null && pas.length > 0) {
                return Stream.of(pas).map(p -> {
                            final String name = p.name(), value = p.value();
                            if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(value)) {
                                return Pair.of(name, value);
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (n, o) -> n));
            }
        }
        return null;
    }

    private boolean isNotFilterObject(@Nonnull final Object o) {
        final Class<?> cls = o.getClass();
        //数组
        if (cls.isArray()) {
            return !cls.getComponentType().isAssignableFrom(MultipartFile.class);
        }
        //集合
        if (Collection.class.isAssignableFrom(cls)) {
            final Collection<?> collection = (Collection<?>) o;
            for (Object val : collection) {
                return !(val instanceof MultipartFile);
            }
        }
        //Map
        if (Map.class.isAssignableFrom(cls)) {
            final Map<?, ?> map = (Map<?, ?>) o;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof MultipartFile) {
                    return false;
                }
                return !(entry.getValue() instanceof MultipartFile);
            }
        }
        return !(o instanceof MultipartFile) && !(o instanceof HttpServletRequest) && !(o instanceof HttpServletResponse) && !(o instanceof BindingResult);
    }

    private void buildParamArgValHandler(@Nonnull final String argName, @Nullable final String argTitle, @Nonnull final Object argVal,
                                         @Nullable final Annotation[] argAnnos, @Nonnull final OperaLog log, @Nonnull final OperaLogDTO operLog,
                                         @Nonnull final Map<String, LogReqParamVal> argParamValMaps) {
        final List<OperaType> types = Lists.newArrayList(OperaType.Add, OperaType.Modify, OperaType.Del);
        //是否为主键记录
        final boolean hasPrimary = types.contains(log.operaType());
        final String primaryKey = Strings.isNullOrEmpty(log.primaryKey()) ? "id" : log.primaryKey();
        //
        final Class<?> cls = argVal.getClass();
        //判断是否为基本类型
        if (isPrimitive(cls) || cls.isArray() || Collection.class.isAssignableFrom(cls)) {
            final LogReqParamVal rpv = LogReqParamVal.of(argTitle, argVal);
            if (Strings.isNullOrEmpty(rpv.getTitle()) && argAnnos != null && argAnnos.length > 0) {
                //注解判断
                for (Annotation anno : argAnnos) {
                    //Swgger注解
                    if (anno instanceof ApiModelProperty) {
                        final ApiModelProperty property = (ApiModelProperty) anno;
                        if (!Strings.isNullOrEmpty(property.value())) {
                            rpv.setTitle(property.value());
                            break;
                        }
                    }
                }
            }
            //检查主键值
            if (hasPrimary && primaryKey.equalsIgnoreCase(argName)) {
                operLog.setPrimaryKeyVal(getPrimaryVal(argVal));
            }
            //保存参数
            if (!Strings.isNullOrEmpty(argName) && !Strings.isNullOrEmpty(rpv.getTitle())) {
                argParamValMaps.put(argName, rpv);
            }
        } else {
            final List<Field> fields = Lists.newLinkedList();
            ClassUtils.getAllFieldsWithSuper(cls, fields);
            if (!CollectionUtils.isEmpty(fields)) {
                fields.stream().filter(Objects::nonNull)
                        .forEach(field -> {
                            try {
                                //设置可见性
                                field.setAccessible(true);
                                //获取对象值
                                final Object val = field.get(argVal);
                                if (val != null && isNotFilterObject(val)) {
                                    buildParamArgValHandler(field.getName(), null, val, field.getAnnotations(), log, operLog, argParamValMaps);
                                }
                            } catch (Throwable ex) {
                                OperaLogAspect.log.warn("buildParamArgVal[arg: {},field: {}]-exp: {}", argVal, field, ex.getMessage());
                            }
                        });
            }
        }
    }

    private String getPrimaryVal(@Nonnull final Object arg) {
        final Class<?> cls = arg.getClass();
        if (cls.isPrimitive()) {
            return arg + "";
        }
        if (arg instanceof String) {
            final String val = (String) arg;
            if (!Strings.isNullOrEmpty(val)) {
                return val;
            }
        }
        return JsonUtils.toJson(objMapper, arg);
    }
}
