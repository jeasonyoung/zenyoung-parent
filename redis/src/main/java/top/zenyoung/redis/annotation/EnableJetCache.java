package top.zenyoung.redis.annotation;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 启用JetCache缓存
 *
 * @author young
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableCreateCacheAnnotation
@EnableMethodCache(basePackages = {"top.zenyoung"})
public @interface EnableJetCache {
    /**
     * 扫描包集合
     *
     * @return 扫描包集合
     */
    @AliasFor(
            annotation = EnableMethodCache.class,
            attribute = "basePackages"
    )
    String[] basePackages();
}
