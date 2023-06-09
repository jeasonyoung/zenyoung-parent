package top.zenyoung.netty.util;

import com.google.common.base.Strings;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
                                                          @Nonnull final Map<String, String> codecMap,
                                                          @Nullable final Boolean checkScopePrototype) {
        return codecMap.entrySet().stream()
                .filter(entry -> !Strings.isNullOrEmpty(entry.getKey()) && !Strings.isNullOrEmpty(entry.getValue()))
                .map(entry -> {
                    final String key = entry.getKey(), val = entry.getValue();
                    final ChannelHandler handler = context.getBean(val, ChannelHandler.class);
                    //检查编解码器注解
                    if (Objects.nonNull(checkScopePrototype) && checkScopePrototype) {
                        ScopeUtils.checkPrototype(handler.getClass());
                    }
                    return Pair.of(key, handler);
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (o, n) -> n));
    }
}
