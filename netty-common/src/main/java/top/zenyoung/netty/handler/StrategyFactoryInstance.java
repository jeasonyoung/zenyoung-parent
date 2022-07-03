package top.zenyoung.netty.handler;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 策略工厂实现基类
 *
 * @author young
 */
@Slf4j
public class StrategyFactoryInstance implements StrategyFactory {
    private static final String SEP = ",";
    private final Map<String, List<BaseStrategyHandler<? extends Message>>> strategyMap;

    public static StrategyFactory instance(@Nullable final List<? extends BaseStrategyHandler<? extends Message>> handlers) {
        return new StrategyFactoryInstance(handlers);
    }

    private StrategyFactoryInstance(@Nullable final List<? extends BaseStrategyHandler<? extends Message>> handlers) {
        this.strategyMap = Objects.isNull(handlers) ? Maps.newHashMap() :
                handlers.stream()
                        .map(handler -> {
                            final String cmd;
                            if (!Strings.isNullOrEmpty(cmd = handler.getCommand())) {
                                if (cmd.contains(SEP)) {
                                    return Splitter.on(SEP).omitEmptyStrings().trimResults()
                                            .splitToList(cmd)
                                            .stream()
                                            .filter(c -> !Strings.isNullOrEmpty(c))
                                            .distinct()
                                            .map(c -> Pair.of(c, handler))
                                            .collect(Collectors.toList());
                                }
                                return Lists.newArrayList(Pair.of(cmd, handler));
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toMap(Pair::getLeft,
                                p -> Lists.newArrayList(p.getRight()),
                                (v1, v2) -> {
                                    v1.addAll(v2);
                                    return v1;
                                }
                        ));
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends Message> T process(@Nonnull final Session session, @Nonnull final T req) {
        log.debug("process(session: {},req: {})", session, req);
        final String command;
        if (!Strings.isNullOrEmpty(command = req.getCommand()) && Objects.nonNull(this.strategyMap) && !this.strategyMap.isEmpty()) {
            final List<BaseStrategyHandler<? extends Message>> items = this.strategyMap.getOrDefault(command, Lists.newArrayList());
            if (Objects.nonNull(items) && !items.isEmpty()) {
                final BaseStrategyHandler<T> strategy = items.stream()
                        .sorted(Comparator.comparing(BaseStrategyHandler::priority, Comparator.reverseOrder()))
                        .map(item -> {
                            final BaseStrategyHandler<T> handler = (BaseStrategyHandler<T>) item;
                            if (handler.supported(req)) {
                                return handler;
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .findFirst().orElse(null);
                if (Objects.nonNull(strategy)) {
                    log.info("process(command: {},session: {},req: {})[命令策略处理器]=> {}", command, session, req, strategy);
                    return strategy.process(session, req);
                }
            }
            log.warn("消息指令未找到策略执行器[{}]=> {}", command, req);
        }
        return null;
    }
}
