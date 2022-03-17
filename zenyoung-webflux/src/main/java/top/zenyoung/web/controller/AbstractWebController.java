package top.zenyoung.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.web.ExceptHandler;
import top.zenyoung.web.listener.ExceptHandlerListener;
import top.zenyoung.web.vo.RespResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Validator;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Web控制器基类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/9 11:19 下午
 **/
public abstract class AbstractWebController implements ParamHandler {

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private ObjectMapper objectMapper;

    @Autowired
    private Validator validator;

    /**
     * 获取异常处理器集合
     *
     * @return 异常处理器集合
     */
    @Nonnull
    protected List<ExceptHandler> getExceptHandlers() {
        return Lists.newLinkedList();
    }

    /**
     * 非异常处理
     *
     * @param respResult 响应结果
     * @param e          异常
     * @param listener   异常处理监听器
     * @return 处理结果
     */
    protected boolean handlerNotExcept(
            @Nonnull final RespResult<?> respResult,
            @Nullable final Throwable e,
            @Nonnull final ExceptHandlerListener listener
    ) {
        //获取异常处理器集合
        final List<ExceptHandler> exceptHandlers = getExceptHandlers();
        if (exceptHandlers.size() > 0) {
            //添加异常处理集合
            listener.getExceptHandlers(exceptHandlers);
        }
        if (e != null && exceptHandlers.size() > 0) {
            final Map<Class<? extends Throwable>, ExceptHandler> handlerMap = exceptHandlers.stream()
                    .collect(Collectors.toMap(ExceptHandler::getEClass, handler -> handler, (n, o) -> n));
            if (handlerMap.size() > 0) {
                return handlerNotExceptCause(respResult, e, handlerMap);
            }
        }
        return true;
    }

    private boolean handlerNotExceptCause(
            @Nonnull final RespResult<?> respResult,
            @Nonnull final Throwable e,
            @Nonnull final Map<Class<? extends Throwable>, ExceptHandler> handlerMap
    ) {
        final ExceptHandler handler = handlerMap.getOrDefault(e.getClass(), null);
        if (handler != null) {
            respResult.buildResp(handler.getCode(), e.getMessage());
            return false;
        }
        final Throwable cause = e.getCause();
        if (cause != null) {
            return handlerNotExceptCause(respResult, cause, handlerMap);
        }
        return true;
    }

    @SneakyThrows
    @Override
    public String serialize(@Nonnull final Map<String, Serializable> params) {
        return objectMapper == null ? null : objectMapper.writeValueAsString(params);
    }

    @Override
    public <T extends Serializable> T deserialize(@Nonnull final String json, @Nonnull final Class<T> paramClass) {
        if (!Strings.isNullOrEmpty(json) && objectMapper != null) {
            return JsonUtils.fromJson(objectMapper, json, paramClass);
        }
        return null;
    }

    @Override
    public <T extends Serializable> void paramValidator(@Nonnull final T req) throws Exception {
        if (validator != null) {
            final SpringValidatorAdapter adapter = new SpringValidatorAdapter(validator);
            final MapBindingResult errors = new MapBindingResult(Maps.newLinkedHashMap(), "params");
            adapter.validate(req, errors);
            //检查是否验证失败
            if (errors.hasErrors()) {
                throw new BindException(errors);
            }
        }
    }

}
