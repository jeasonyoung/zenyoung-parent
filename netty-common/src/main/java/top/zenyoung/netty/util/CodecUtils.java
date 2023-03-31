package top.zenyoung.netty.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.codec.MessageCodec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 编解码器工具类
 *
 * @author young
 */
@Slf4j
public class CodecUtils {

    public static Map<String, ChannelHandler> getCodecMap(@Nonnull final ApplicationContext context,
                                                          @Nullable final Map<String, String> codecMap,
                                                          @Nullable final Boolean checkScopePrototype) {
        //1.从配置获取编解码配置
        return Optional.ofNullable(getChannelHandlersFromArgs(context, codecMap, checkScopePrototype))
                .filter(handlers -> !CollectionUtils.isEmpty(handlers))
                .orElseGet(() -> {
                    //2.从上下文中加载编解码器
                    return Optional.ofNullable(getChannelHandlers(context, checkScopePrototype))
                            .orElse(Maps.newHashMap());
                });
    }

    private static Map<String, ChannelHandler> getChannelHandlersFromArgs(@Nonnull final ApplicationContext context,
                                                                          @Nullable final Map<String, String> codecMap,
                                                                          @Nullable final Boolean checkScopePrototype) {
        return Optional.ofNullable(codecMap)
                .map(Map::entrySet)
                .map(Set::stream)
                .map(stream -> stream.map(entry -> {
                                    final String key = entry.getKey(), val = entry.getValue();
                                    if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(val)) {
                                        return null;
                                    }
                                    return Optional.of(context.getBean(val))
                                            .filter(bean -> bean instanceof ChannelHandler)
                                            .map(bean -> {
                                                //检查编解码器注解
                                                if (Objects.nonNull(checkScopePrototype) && checkScopePrototype) {
                                                    ScopeUtils.checkPrototype(bean.getClass());
                                                }
                                                return Pair.of(key, (ChannelHandler) bean);
                                            })
                                            .orElseGet(() -> Optional.ofNullable(createHandler(() -> Class.forName(val)))
                                                    .filter(ChannelHandler.class::isAssignableFrom)
                                                    .map(cls -> createHandler(() -> (ChannelHandler) cls.newInstance()))
                                                    .map(handler -> Pair.of(key, handler))
                                                    .orElse(null)
                                            );
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(Pair::getLeft, Pair::getValue, (o, n) -> n))
                )
                .filter(map -> !CollectionUtils.isEmpty(map))
                .orElse(null);
    }

    private static Map<String, ChannelHandler> getChannelHandlers(@Nonnull final ApplicationContext context,
                                                                  @Nullable final Boolean checkScopePrototype) {

        return Optional.of(context.getBeansOfType(MessageCodec.class))
                .filter(map -> !CollectionUtils.isEmpty(map))
                .map(Map::entrySet)
                .map(Set::stream)
                .map(stream -> stream.map(entry -> {
                                    final String key = entry.getKey();
                                    final Object val = entry.getValue();
                                    if (Strings.isNullOrEmpty(key) || Objects.isNull(val)) {
                                        return null;
                                    }
                                    return Optional.of(val.getClass())
                                            .filter(ChannelHandler.class::isAssignableFrom)
                                            .map(cls -> {
                                                //检查编解码器注解
                                                if (Objects.nonNull(checkScopePrototype) && checkScopePrototype) {
                                                    ScopeUtils.checkPrototype(cls);
                                                }
                                                return Pair.of(key, (ChannelHandler) val);
                                            })
                                            .orElse(null);
                                })
                                .filter(Objects::nonNull)
                                .sorted(Comparator.comparing(Pair::getLeft))
                                .collect(Collectors.toMap(Pair::getLeft, Pair::getValue, (n, o) -> n))
                )
                .orElse(null);
    }


    private static <T> T createHandler(@Nonnull final InnerSupplier<T> handler) {
        try {
            return handler.get();
        } catch (Throwable e) {
            log.error("createHandler(handler: {})-exp: {}", handler, e.getMessage());
            return null;
        }
    }

    @FunctionalInterface
    private interface InnerSupplier<T> {

        T get() throws Throwable;
    }
}
