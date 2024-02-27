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
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class StrategyUtils {
    public static <M extends Message, H extends StrategyHandler<M>> StrategyHandlerFactory build(@Nullable final Collection<H> strategyHandlers) {
        return new StrategyHandlerFactoryInner<>(strategyHandlers);
    }

    private static class StrategyHandlerFactoryInner<M extends Message, H extends StrategyHandler<M>> implements StrategyHandlerFactory {
        private final Map<String, List<H>> commandStrategyHandlers = Maps.newHashMap();

        public StrategyHandlerFactoryInner(@Nullable final Collection<H> strategyHandlers) {
            if (CollectionUtils.isEmpty(strategyHandlers)) {
                return;
            }
            buildHanlder(strategyHandlers);
        }

        private void buildHanlder(@Nonnull final Collection<H> strategyHandlers) {
            final Map<String, List<H>> cmdStrategyHandlers = strategyHandlers.stream()
                    .filter(Objects::nonNull)
                    .map(handler -> Stream.of(handler.getCommands())
                            .filter(cmd -> !Strings.isNullOrEmpty(cmd))
                            .distinct()
                            .map(cmd -> {
                                log.info("注册[策略处理器: {}]=> {}", cmd, handler);
                                return Pair.of(cmd, handler);
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
        @SuppressWarnings({"unchecked"})
        public <T extends Message> void process(@Nonnull final Session session, @Nonnull final T req, @Nonnull final Consumer<T> callbackHandler) {
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
                        if (!handler.supported(session, (M) req)) {
                            log.warn("process[command: {}]-不支持处理=> {}", command, handler);
                            return;
                        }
                        //业务处理
                        log.info("process[command: {}]-策略处理器开始处理业务=> {}", command, handler);
                        final M callback = handler.process(session, (M) req);
                        if (Objects.nonNull(callback)) {
                            callbackHandler.accept((T) callback);
                        }
                    });
        }
    }
}
