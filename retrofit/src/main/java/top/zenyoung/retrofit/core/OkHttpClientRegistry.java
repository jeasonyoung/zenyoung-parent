package top.zenyoung.retrofit.core;

import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.retrofit.OkHttpClientRegistrar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * OkHttpClient-注册中心
 *
 * @author young
 */
@Slf4j
public class OkHttpClientRegistry {
    private final Map<String, OkHttpClient> okHttpClientMap;
    private final List<OkHttpClientRegistrar> registrars;

    /**
     * 构造函数
     *
     * @param registrars 注册器集合
     */
    public OkHttpClientRegistry(@Nullable final List<OkHttpClientRegistrar> registrars) {
        this.registrars = registrars;
        this.okHttpClientMap = Maps.newHashMap();
    }

    @PostConstruct
    public void init() {
        log.info("init...");
        if (!CollectionUtils.isEmpty(registrars)) {
            registrars.forEach(registrar -> registrar.register(this));
        }
    }

    public void register(@Nonnull final String name, @Nonnull final OkHttpClient client) {
        Assert.hasText(name, "'name'不能为空");
        okHttpClientMap.put(name, client);
    }

    public OkHttpClient get(@Nonnull final String name) {
        Assert.hasText(name, "'name'不能为空");
        return okHttpClientMap.getOrDefault(name, null);
    }
}
