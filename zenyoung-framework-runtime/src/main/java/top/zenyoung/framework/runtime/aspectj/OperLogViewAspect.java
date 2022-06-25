package top.zenyoung.framework.runtime.aspectj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.common.util.ClassUtils;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.framework.annotation.OperLogView;
import top.zenyoung.framework.annotation.OperLogViewFieldValue;
import top.zenyoung.framework.common.LogViewFieldType;
import top.zenyoung.framework.runtime.model.LogReqParamVal;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 操作日志回显-切面
 *
 * @author young
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogViewAspect extends BaseAspect {
    private static final Cache<String, LogReqParamVal> VAL_CACHE = CacheUtils.createCache(500, Duration.ofMinutes(5));
    private static final String METHOD_KEY = "method";
    private final ObjectMapper objMapper;

    private final ApplicationContext context;

    @AfterReturning(pointcut = "@annotation(controllerLogView)", returning = "jsonResult")
    public void doAfterReturning(final JoinPoint joinPoint, final OperLogView controllerLogView, final Object jsonResult) {
        final long start = System.currentTimeMillis();
        try {
            final String logViewFieldName;
            if (jsonResult != null && !Strings.isNullOrEmpty(logViewFieldName = controllerLogView.value())) {
                final Class<?> retCls = jsonResult.getClass();
                final Field reqMethod = ClassUtils.findFieldByClass(retCls, f -> METHOD_KEY.equalsIgnoreCase(f.getName()));
                if (reqMethod != null) {
                    final Object methodObj = reqMethod.get(jsonResult);
                    if (methodObj instanceof String) {
                        final String method = methodObj + "";
                        if (!Strings.isNullOrEmpty(method)) {
                            final Field reqDataField = ClassUtils.findFieldByClass(retCls, f -> logViewFieldName.equalsIgnoreCase(f.getName()));
                            if (reqDataField != null) {
                                final Object oldVal = reqDataField.get(jsonResult);
                                if (oldVal != null) {
                                    final Map<String, LogReqParamVal> dataMap = JsonUtils.toMap(objMapper, oldVal, LogReqParamVal.class);
                                    if (!CollectionUtils.isEmpty(dataMap)) {
                                        //业务处理后的值
                                        final Object newVal = dataConvertHandler(method, dataMap);
                                        if (!oldVal.equals(newVal)) {
                                            //重新赋值处理
                                            reqDataField.set(jsonResult, newVal);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            log.error("doAfterReturning(joinPoint: {},controllerLogView: {},jsonResult: {})-exp: {}", joinPoint, controllerLogView, jsonResult, ex.getMessage());
        } finally {
            log.info("doAfterReturning-执行时长: {}ms", (System.currentTimeMillis() - start));
        }
    }

    @SneakyThrows({})
    private Object dataConvertHandler(@Nonnull final String method, @Nonnull final Map<String, LogReqParamVal> dataMap) {
        final String sep = ".";
        final int lastIdx = method.lastIndexOf(sep);
        if (lastIdx > 0) {
            final String className = method.substring(0, lastIdx), methodName = method.substring(lastIdx + 1).replace("()", "");
            if (!Strings.isNullOrEmpty(className) && !Strings.isNullOrEmpty(methodName)) {
                final Class<?> cls = Class.forName(className);
                final Optional<Method> optMethod = Stream.of(cls.getDeclaredMethods()).filter(d -> methodName.startsWith(d.getName())).findFirst();
                if (optMethod.isPresent()) {
                    final Parameter[] params = optMethod.get().getParameters();
                    if (params != null && params.length > 0) {
                        final String key = className + "." + methodName;
                        dataMap.forEach((name, val) -> {
                            if (!Strings.isNullOrEmpty(name)) {
                                final LogReqParamVal ret = getFieldValHandler(key, params, name, val);
                                if (!val.equals(ret)) {
                                    dataMap.put(name, ret);
                                }
                            }
                        });
                    }
                }
            }
        }
        return dataMap;
    }

    private LogReqParamVal getFieldValHandler(@Nonnull final String key, @Nonnull final Parameter[] params, @Nonnull final String name, @Nonnull final LogReqParamVal paramVal) {
        for (Parameter p : params) {
            final Class<?> pc = p.getType();
            if (isPrimitive(pc)) {
                if (name.equalsIgnoreCase(p.getName()) && p.isAnnotationPresent(OperLogViewFieldValue.class)) {
                    return convertDataVal(key + "." + p.getName(), p.getAnnotation(OperLogViewFieldValue.class), paramVal);
                }
            } else {
                final List<Field> fields = Lists.newLinkedList();
                ClassUtils.getAllFieldsWithSuper(pc, fields);
                if (!CollectionUtils.isEmpty(fields)) {
                    for (Field f : fields) {
                        if (name.equalsIgnoreCase(f.getName())) {
                            if (f.isAnnotationPresent(OperLogViewFieldValue.class)) {
                                return convertDataVal(key + "." + f.getName(), f.getAnnotation(OperLogViewFieldValue.class), paramVal);
                            }
                            return paramVal;
                        }
                    }
                }
            }
        }
        return paramVal;
    }

    private LogReqParamVal cacheHandler(@Nonnull final Boolean isCache, @Nonnull final String key, @Nonnull final Supplier<LogReqParamVal> handler) {
        if (isCache && !Strings.isNullOrEmpty(key)) {
            LogReqParamVal data = VAL_CACHE.getIfPresent(key);
            if (data == null) {
                data = handler.get();
                if (data != null) {
                    VAL_CACHE.put(key, data);
                }
            }
            return data;
        }
        return handler.get();
    }

    private LogReqParamVal convertDataVal(@Nonnull final String key, @Nonnull final OperLogViewFieldValue fieldValue, @Nonnull final LogReqParamVal paramVal) {
        final LogViewFieldType fieldType = fieldValue.type();
        if (fieldType == LogViewFieldType.Ignore) {
            return null;
        }
        final String cacheKey = key + "_" + paramVal.getVal();
        return cacheHandler(fieldValue.cache(), cacheKey, () -> {
            //日期格式
            if (fieldType == LogViewFieldType.Date) {
                final int maxLen = 10;
                final String strVal = paramVal.getVal() + "";
                if (!Strings.isNullOrEmpty(strVal) && strVal.length() > maxLen) {
                    paramVal.setVal(strVal.substring(0, maxLen));
                }
                return paramVal;
            }
            //字典
            if (fieldType == LogViewFieldType.Dict) {
                ///TODO: 字典处理
            }
            //上传
            if (fieldType == LogViewFieldType.Download) {
                ///TODO: 文件上传处理
            }
            //业务数据
            if (fieldType == LogViewFieldType.Biz) {
                final String methodName;
                final Class<?> beanCls;
                if ((beanCls = fieldValue.beanClass()) != null && !Strings.isNullOrEmpty(methodName = fieldValue.method())) {
                    final Object provider = context.getBean(beanCls);
                    final Class<?> pCls = provider.getClass();
                    final Method method = Stream.of(pCls.getDeclaredMethods()).filter(m -> methodName.equalsIgnoreCase(m.getName())).findFirst().orElse(null);
                    if (method != null) {
                        try {
                            final Object data = method.invoke(provider, paramVal.getVal());
                            if (!paramVal.getVal().equals(data)) {
                                paramVal.setVal(data);
                                return paramVal;
                            }
                        } catch (Throwable ex) {
                            log.error("convertDataVal(key: {},fieldValue: {}, paramVal: {})-exp: {}", key, fieldValue, paramVal, ex.getMessage());
                        }
                    }
                }
            }
            return paramVal;
        });
    }
}
