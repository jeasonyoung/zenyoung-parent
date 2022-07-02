package top.zenyoung.netty.server.server.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.server.handler.StrategyHandler;
import top.zenyoung.netty.server.server.StrategyFactory;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 命令策略工厂接口实现
 *
 * @author young
 */
@Slf4j
public class StrategyFactoryImpl implements StrategyFactory {
    private final Map<String, List<StrategyHandler<? extends Message>>> strategyMap;

    /**
     * 构造函数
     *
     * @param strategies 命令策略处理器集合
     */
    public StrategyFactoryImpl(@Nullable final List<StrategyHandler<? extends Message>> strategies) {
        final String sep = ",";
        this.strategyMap = CollectionUtils.isEmpty(strategies) ? Maps.newHashMap() :
                strategies.stream()
                        .map(s -> {
                            final String cmd;
                            if (!Strings.isNullOrEmpty(cmd = s.getCommand())) {
                                if (cmd.contains(sep)) {
                                    return Splitter.on(sep).omitEmptyStrings().trimResults()
                                            .splitToList(cmd)
                                            .stream()
                                            .filter(c -> !Strings.isNullOrEmpty(c))
                                            .distinct()
                                            .map(c -> Pair.of(c, s))
                                            .collect(Collectors.toList());
                                }
                                return Lists.newArrayList(Pair.of(cmd, s));
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
        if (!Strings.isNullOrEmpty(command = req.getCommand()) && !CollectionUtils.isEmpty(this.strategyMap)) {
            final List<StrategyHandler<? extends Message>> items = this.strategyMap.getOrDefault(command, Lists.newArrayList());
            if (!CollectionUtils.isEmpty(items)) {
                final StrategyHandler<T> strategy = items.stream()
                        .sorted(Comparator.comparing(StrategyHandler::priority, Comparator.reverseOrder()))
                        .map(item -> {
                            final StrategyHandler<T> handler = (StrategyHandler<T>) item;
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
