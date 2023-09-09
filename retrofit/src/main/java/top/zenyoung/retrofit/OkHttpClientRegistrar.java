package top.zenyoung.retrofit;

import top.zenyoung.retrofit.core.OkHttpClientRegistry;

import javax.annotation.Nonnull;

/**
 * OkHttpClient-注册器
 *
 * @author young
 */
public interface OkHttpClientRegistrar {

    /**
     * 注册数据
     *
     * @param registry 注册对象
     */
    void register(@Nonnull final OkHttpClientRegistry registry);
}
