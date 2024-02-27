package top.zenyoung.netty.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;
import top.zenyoung.netty.strategy.StrategyHandler;
import top.zenyoung.netty.strategy.StrategyHandlerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class StrategyUtils {
    public static <T extends StrategyHandler> StrategyHandlerFactory build(@Nonnull final Collection<T> strategyHandlers) {
        return new StrategyHandlerFactoryInner(strategyHandlers);
    }

    private static class StrategyHandlerFactoryInner implements StrategyHandlerFactory {
        private final Map<String, List<StrategyHandler>> commandStrategyHandlers = Maps.newHashMap();

        public StrategyHandlerFactoryInner(@Nonnull final Collection<? extends StrategyHandler> strategyHandlers) {
            if (CollectionUtils.isEmpty(strategyHandlers)) {
                return;
            }
            buildHanlder(strategyHandlers);
        }

        private void buildHanlder(@Nonnull final Collection<? extends StrategyHandler> strategyHandlers) {
            final Map<String, List<StrategyHandler>> cmdStrategyHandlers = strategyHandlers.stream()
                    .filter(Objects::nonNull)
                    .map(handler -> Stream.of(handler.getCommands())
                            .filter(cmd -> !Strings.isNullOrEmpty(cmd))
                            .distinct()
                            .map(cmd -> {
                                log.info("注册[策略处理器: {}]=> {}", cmd, handler);
                                return Pair.<String, StrategyHandler>of(cmd, handler);
                            })
                            .toList()
                    )
                    .flatMap(Collection::stream)
                    .collect(Collectors.groupingBy(Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));
            if (!CollectionUtils.isEmpty(cmdStrategyHandlers)) {
                commandStrategyHandlers.putAll(cmdStrategyHandlers);
            }
        }

        @Override
        public void process(@Nonnull final Session session, @Nonnull final Message req, @Nonnull final Consumer<Message> callbackHandler) {
            final String command = req.getCommand();
            if (Strings.isNullOrEmpty(command)) {
                return;
            }
            final var handlers = commandStrategyHandlers.getOrDefault(command, null);
            if (CollectionUtils.isEmpty(handlers)) {
                log.warn("process[command: {}]- 未找到命令处理器.", command);
                return;
            }
            handlers.stream()
                    .sorted(Comparator.comparing(StrategyHandler::priority, Comparator.reverseOrder()))
                    .distinct()
                    .forEach(handler -> {
                        //判断是否支持
                        if (!handler.supported(session, req)) {
                            log.warn("process[command: {}]-不支持处理=> {}", command, handler);
                            return;
                        }
                        //业务处理
                        log.info("process[command: {}]-策略处理器开始处理业务=> {}", command, handler);
                        final Message callback = handler.process(session, req);
                        if (Objects.nonNull(callback)) {
                            callbackHandler.accept(callback);
                        }
                    });
        }
    }
}
