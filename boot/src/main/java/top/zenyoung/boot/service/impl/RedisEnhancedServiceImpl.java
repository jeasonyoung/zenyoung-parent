package top.zenyoung.boot.service.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.zenyoung.boot.service.RedisEnhancedService;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Redis增强-服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
public class RedisEnhancedServiceImpl implements RedisEnhancedService {
    private final static String INCLUDE_PREFIX = "top.zenyoung.";

    @Override
    public void redisHandler(@Nonnull final Runnable handler) {
        try {
            handler.run();
        } catch (Throwable e) {
            log.error("redisHandler(handler: {})-exp: {}", handler, formatPrintInfo(e));
        }
    }

    @Override
    public <T> void redisHandler(@Nonnull final T data, @Nonnull final Consumer<T> handler) {
        try {
            handler.accept(data);
        } catch (Throwable e) {
            log.error("redisHandler(data: {}, handler: {})-exp: {}", data, handler, formatPrintInfo(e));
        }
    }

    @Override
    public <T> T redisHandler(@Nonnull final Supplier<T> handler) {
        try {
            return handler.get();
        } catch (Throwable e) {
            log.error("redisHandler(handler: {})-exp: {}", handler, formatPrintInfo(e));
        }
        return null;
    }

    private String formatPrintInfo(final Throwable e) {
        final StringBuilder sb = new StringBuilder(e.getMessage());
        final StackTraceElement[] stackTraces = e.getStackTrace();
        final int totals, startIdx = 1;
        if (stackTraces != null && (totals = stackTraces.length) > 0 && !Strings.isNullOrEmpty(INCLUDE_PREFIX)) {
            for (int idx = startIdx; idx < totals; idx++) {
                final StackTraceElement el = stackTraces[idx];
                if (Objects.nonNull(el)) {
                    final String clsName = el.getClassName();
                    if (Strings.isNullOrEmpty(clsName) || clsName.startsWith(getClass().getName())) {
                        continue;
                    }
                    if (clsName.startsWith(INCLUDE_PREFIX)) {
                        sb.append("=>").append(clsName).append(".").append(el.getMethodName());
                        break;
                    }
                }
            }
        }
        return sb.toString();
    }
}
