package top.zenyoung.netty.strategy;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.BaseStrategyHandler;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 策略处理器工厂
 *
 * @author young
 */
@Slf4j
public class StrategyHandlerFactory {
    private final Map<String, List<BaseStrategyHandler<? extends Message>>> commandStrategyHandlers;

    /**
     * 构造函数
     *
     * @param handlers 策略处理器集合
     */
    public StrategyHandlerFactory(final List<BaseStrategyHandler<? extends Message>> handlers) {
        this.commandStrategyHandlers = buildCommandStrategyHandlers(handlers);
    }

    private Map<String, List<BaseStrategyHandler<? extends Message>>> buildCommandStrategyHandlers(
            @Nullable final List<BaseStrategyHandler<? extends Message>> handlers) {
        if (!CollectionUtils.isEmpty(handlers)) {
            final List<CommandStrategyHandler> items = handlers.stream()
                    .map(handler -> {
                        final String[] commands = handler.getCommands();
                        if (ArrayUtils.isEmpty(commands)) {
                            return null;
                        }
                        return Stream.of(commands)
                                .filter(command -> !Strings.isNullOrEmpty(command))
                                .distinct()
                                .map(command -> {
                                    log.info("注册[策略处理器: {}]=> {}", command, handler);
                                    return CommandStrategyHandler.of(command, handler);
                                })
                                .collect(Collectors.toList());
                    })
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(items)) {
                return items.stream()
                        .collect(Collectors.groupingBy(
                                CommandStrategyHandler::getCommand,
                                Collectors.mapping(CommandStrategyHandler::getHandler, Collectors.toList())
                        ));
            }
        }
        return Maps.newHashMap();
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Message> void process(@Nonnull final Session session, @Nonnull final T req,
                                            @Nonnull final Consumer<T> callbackHandler) {
        final String command = req.getCommand();
        Assert.hasText(command, "'req.command'不能为空");
        final List<BaseStrategyHandler<? extends Message>> handlers = commandStrategyHandlers.getOrDefault(command, null);
        if (CollectionUtils.isEmpty(handlers)) {
            log.warn("process[command: {}]- 未找到命令处理器.", command);
            return;
        }
        handlers.stream()
                .sorted(Comparator.comparing(BaseStrategyHandler::priority, Comparator.reverseOrder()))
                .map(handler -> (BaseStrategyHandler<T>) handler)
                .distinct()
                .forEach(handler -> {
                    //判断是否支持
                    if (!handler.supported(req)) {
                        log.warn("process[command: {}]-不支持处理=> {}", command, handler);
                        return;
                    }
                    //业务处理
                    log.info("process[command: {}]-策略处理器开始处理业务=> {}", command, handler);
                    final T callback = handler.process(session, req);
                    if (Objects.nonNull(callback)) {
                        callbackHandler.accept(callback);
                    }
                });
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    private static class CommandStrategyHandler {
        private final String command;
        private final BaseStrategyHandler<? extends Message> handler;
    }
}
