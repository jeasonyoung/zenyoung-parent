package top.zenyoung.netty.handler;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 策略工厂实现基类
 *
 * @author young
 */
@Slf4j
public class StrategyFactoryInstance implements StrategyFactory {
    private final Map<String, List<BaseStrategyHandler<? extends Message>>> strategyMap;

    private StrategyFactoryInstance(@Nullable final List<? extends BaseStrategyHandler<? extends Message>> handlers) {
        this.strategyMap = Optional.ofNullable(handlers)
                .filter(items -> !CollectionUtils.isEmpty(items))
                .map(items -> items.stream()
                        .map(item -> Optional.ofNullable(item.getCommands())
                                .filter(ArrayUtils::isNotEmpty)
                                .map(Stream::of)
                                .map(stream -> stream.filter(cmd -> !Strings.isNullOrEmpty(cmd)))
                                .map(Stream::distinct)
                                .map(stream -> stream.map(cmd -> Pair.of(cmd, item)))
                                .map(stream -> stream.collect(Collectors.toList()))
                                .orElse(Lists.newArrayList())
                        )
                        .flatMap(Collection::stream)
                        .collect(Collectors.toMap(Pair::getLeft,
                                        p -> {
                                            final List<BaseStrategyHandler<? extends Message>> rows = Lists.newArrayList();
                                            rows.add(p.getRight());
                                            return rows;
                                        },
                                        (v1, v2) -> {
                                            v1.addAll(v2);
                                            return v1;
                                        }
                                )
                        )
                )
                .orElse(Maps.newHashMap());
    }

    public static StrategyFactory instance(@Nullable final List<? extends BaseStrategyHandler<? extends Message>> handlers) {
        return new StrategyFactoryInstance(handlers);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends Message> T process(@Nonnull final Session session, @Nonnull final T req) {
        log.debug("process(session: {},req: {})", session, req);
        final AtomicBoolean refDo = new AtomicBoolean(false);
        return Optional.ofNullable(req.getCommand())
                .filter(cmd -> !Strings.isNullOrEmpty(cmd) && !CollectionUtils.isEmpty(strategyMap))
                .flatMap(cmd -> Optional.ofNullable(strategyMap.getOrDefault(cmd, null))
                        .filter(items -> !CollectionUtils.isEmpty(items))
                        .flatMap(items -> items.stream()
                                .sorted(Comparator.comparing(BaseStrategyHandler::priority, Comparator.reverseOrder()))
                                .map(item -> Optional.of((BaseStrategyHandler<T>) item)
                                        .filter(handler -> handler.supported(req))
                                        .orElse(null)
                                )
                                .filter(Objects::nonNull)
                                .findFirst()
                        )
                        .map(handler -> {
                            refDo.set(true);
                            log.info("process(session: {},req: {})[cmd: {},命令策略处理器]=> {}", session, req, req.getCommand(), handler);
                            return handler.process(session, req);
                        })
                )
                .orElseGet(()->{
                    if (!refDo.get()) {
                        log.warn("消息指令未找到策略执行器[{}]=> {}", req.getCommand(), req);
                    }
                    return null;
                });
    }
}
