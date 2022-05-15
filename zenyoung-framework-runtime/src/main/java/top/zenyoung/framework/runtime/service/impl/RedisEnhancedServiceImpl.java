package top.zenyoung.framework.runtime.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.zenyoung.framework.service.RedisEnhancedService;

import javax.annotation.Nonnull;
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

    @Override
    public void redisHandler(@Nonnull final Runnable handler) {
        try {
            handler.run();
        } catch (Throwable e) {
            log.error("redisHandler(handler: {})-exp: {}", handler, e.getMessage());
        }
    }

    @Override
    public <T> void redisHandler(@Nonnull final T data, @Nonnull final Consumer<T> handler) {
        try {
            handler.accept(data);
        } catch (Throwable e) {
            log.error("redisHandler(data: {}, handler: {})-exp: {}", data, handler, e.getMessage());
        }
    }

    @Override
    public <T> T redisHandler(@Nonnull final Supplier<T> handler) {
        try {
            return handler.get();
        } catch (Throwable e) {
            log.error("redisHandler(handler: {})-exp: {}", handler, e.getMessage());
        }
        return null;
    }
}
