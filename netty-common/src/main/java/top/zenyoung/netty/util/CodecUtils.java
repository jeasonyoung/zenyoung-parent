package top.zenyoung.netty.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.codec.MessageCodec;
import top.zenyoung.netty.prop.BaseProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 编解码器工具类
 *
 * @author young
 */
@Slf4j
public class CodecUtils {

    public static Map<String, ChannelHandler> getCodecMap(@Nonnull final ApplicationContext context,
                                                          @Nonnull final BaseProperties properties,
                                                          @Nullable final Boolean checkScopePrototype) {
        //1.从配置获取编解码配置
        final Map<String, ChannelHandler> propChannelHandlers = getChannelHandlers(context, properties, checkScopePrototype);
        if (!CollectionUtils.isEmpty(propChannelHandlers)) {
            return propChannelHandlers;
        }
        //2.从上下文中加载编解码器
        final Map<String, ChannelHandler> ctxChannelHandlers = getChannelHandlers(context, checkScopePrototype);
        if (!CollectionUtils.isEmpty(ctxChannelHandlers)) {
            return ctxChannelHandlers;
        }
        //空置集合
        return Maps.newHashMap();
    }

    private static Map<String, ChannelHandler> getChannelHandlers(@Nonnull final ApplicationContext context,
                                                                  @Nonnull final BaseProperties properties,
                                                                  @Nullable final Boolean checkScopePrototype) {
        final Map<String, String> codecMap = properties.getCodec();
        if (!CollectionUtils.isEmpty(codecMap)) {
            return codecMap.entrySet().stream()
                    .map(entry -> {
                        final String key = entry.getKey(), val = entry.getValue();
                        if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(val)) {
                            return null;
                        }
                        try {
                            Object handler = context.getBean(val);
                            if (handler instanceof ChannelHandler) {
                                //检查编解码器注解
                                if (Objects.nonNull(checkScopePrototype) && checkScopePrototype) {
                                    ScopeUtils.checkPrototype(handler.getClass());
                                }
                                return Pair.of(key, (ChannelHandler) handler);
                            }
                            final Class<?> cls = Class.forName(val);
                            if (ChannelHandler.class.isAssignableFrom(cls)) {
                                handler = cls.newInstance();
                                return Pair.of(key, (ChannelHandler) handler);
                            }
                        } catch (Throwable e) {
                            log.warn("getCodec[key: {},val: {}]-exp: {}", key, val, e.getMessage());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getValue, (n, o) -> n));
        }
        return null;
    }

    private static Map<String, ChannelHandler> getChannelHandlers(@Nonnull final ApplicationContext context,
                                                                  @Nullable final Boolean checkScopePrototype) {
        try {
            final Map<String, ?> codecMap = context.getBeansOfType(MessageCodec.class);
            if (!CollectionUtils.isEmpty(codecMap)) {
                return codecMap.entrySet().stream()
                        .map(entry -> {
                            final String key = entry.getKey();
                            final Object val = entry.getValue();
                            if (!Strings.isNullOrEmpty(key) && Objects.nonNull(val)) {
                                final Class<?> cls = val.getClass();
                                if (ChannelHandler.class.isAssignableFrom(cls)) {
                                    //检查编解码器注解
                                    if (Objects.nonNull(checkScopePrototype) && checkScopePrototype) {
                                        ScopeUtils.checkPrototype(cls);
                                    }
                                    return Pair.of(key, (ChannelHandler) val);
                                }
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(Pair::getLeft))
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getValue, (n, o) -> n));
            }
        } catch (Throwable e) {
            log.warn("getChannelHandlers-exp: {}", e.getMessage());
        }
        return null;
    }
}
