package top.zenyoung.netty.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.BaseStrategyHandler;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 策略处理器-工具类
 *
 * @author young
 */
@Slf4j
public class StrategyHandlerUtils {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Map<String, List<BaseStrategyHandler<?>>> COMMAND_HANDLERS = Maps.newHashMap();

    /**
     * 注册命令处理器
     *
     * @param command 命令名称
     * @param handler 处理器
     */
    public static void register(@Nonnull final String command, @Nonnull final BaseStrategyHandler<?> handler) {
        Assert.hasText(command, "'command'不能为空");
        synchronized (LOCKS.computeIfAbsent(command, k -> new Object())) {
            try {
                final List<BaseStrategyHandler<?>> handlers = COMMAND_HANDLERS.computeIfAbsent(command, k -> Lists.newArrayList());
                if (CollectionUtils.isEmpty(handlers)) {
                    handlers.add(handler);
                    return;
                }
                if (!handlers.contains(handler)) {
                    handlers.add(handler);
                }
            } finally {
                LOCKS.remove(command);
                log.info("register[command:{}]=> {}", command, handler);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends Message> void process(@Nonnull final Session session, @Nonnull final T req,
                                                   @Nonnull final Consumer<T> callbackHandler) {
        final String command = req.getCommand();
        Assert.hasText(command, "'req.command'不能为空");
        synchronized (LOCKS.computeIfAbsent(command, k -> new Object())) {
            try {
                final List<BaseStrategyHandler<?>> handlers = COMMAND_HANDLERS.getOrDefault(command, null);
                if (CollectionUtils.isEmpty(handlers)) {
                    log.warn("process[command: {}]- 未找到命令处理器.", command);
                    return;
                }
                handlers.stream()
                        .sorted(Comparator.comparing(BaseStrategyHandler::priority, Comparator.reverseOrder()))
                        .map(handler -> (BaseStrategyHandler<T>) handler)
                        .forEach(handler -> {
                            //判断是否支持
                            if (!handler.supported(req)) {
                                log.warn("process[command: {}]-不支持处理=> {}", command, handler);
                                return;
                            }
                            //业务处理
                            final T callback = handler.process(session, req);
                            if (Objects.nonNull(callback)) {
                                callbackHandler.accept(callback);
                            }
                            log.warn("process[command: {}]-处理=> {}", command, handler);
                        });
            } finally {
                LOCKS.remove(command);
            }
        }
    }

}
