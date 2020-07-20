package top.zenyoung.wechat.api;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import io.netty.buffer.UnpooledByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.controller.BaseController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 连接-控制器
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/12 2:58 下午
 **/
@Slf4j
public abstract class BaseConnectController extends BaseController {
    private static final Cache<String, String> APP_TOKEN_CACHE = CacheUtils.createCache(100, 30, TimeUnit.SECONDS);

    private static final String DEF_APP_KEY = "app-key-def";
    private static final String DEF_CALLBACK_BODY = "ok";

    /**
     * 注入-服务器解码器
     */
    @Autowired
    private ServerCodecConfigurer serverCodecConfigurer;

    /**
     * 微信接入
     *
     * @param appKey   接入App键
     * @param request  请求数据
     * @param response 响应数据
     * @return 接入处理
     */
    public Mono<Void> connect(@Nullable final String appKey, @Nonnull final ServerHttpRequest request, @Nonnull final ServerHttpResponse response) {
        log.debug("connect(appKey: {})...", appKey);
        final HttpMethod method = request.getMethod();
        if (method == null) {
            throw new RuntimeException("请求方法不能为null!");
        }
        //数据处理器
        final NettyDataBufferFactory bufferFactory = new NettyDataBufferFactory(new UnpooledByteBufAllocator(false));
        //业务处理
        switch (method) {
            case GET: {
                return response.writeWith(Mono.create(sink -> {
                    try {
                        final String key = Strings.isNullOrEmpty(appKey) ? DEF_APP_KEY : appKey;
                        final String appToken = CacheUtils.getCacheValue(APP_TOKEN_CACHE, key, () -> getAppToken(appKey));
                        Assert.hasText(appToken, "'appToken'不能为空!");
                        //接入处理
                        assert !Strings.isNullOrEmpty(appToken);
                        //业务处理
                        final String ret = connectAuthen(appToken, request.getQueryParams());
                        //返回处理
                        sink.success(bufferFactory.wrap(ret.getBytes(StandardCharsets.UTF_8)));
                    } catch (Throwable ex) {
                        log.warn("connect(appKey: {})-exp: {}", appKey, ex.getMessage());
                        sink.error(ex);
                    }
                }));
            }
            case POST: {
                final ResolvableType reqDataType = ResolvableType.forClass(byte[].class);
                return response.writeWith(serverCodecConfigurer.getReaders().stream()
                        .filter(reader -> reader.canRead(reqDataType, MediaType.ALL))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No Data"))
                        .readMono(reqDataType, request, Collections.emptyMap())
                        .cast(byte[].class)
                        .map(bytes -> {
                            try {
                                final String reqBody = new String(bytes, StandardCharsets.UTF_8);
                                final String callback = connectCallback(appKey, reqBody);
                                log.info("connect(appKey: {}) =>req:\n {} resp:\n {} ", appKey, reqBody, callback);
                                return bufferFactory.wrap((Strings.isNullOrEmpty(callback) ? DEF_CALLBACK_BODY : callback).getBytes(StandardCharsets.UTF_8));
                            } catch (Throwable ex) {
                                log.warn("connect(appKey: {})-exp: {}", appKey, ex.getMessage());
                                return null;
                            }
                        })
                );
            }
            default:
                return Mono.error(new UnsupportedOperationException(method.name()));
        }
    }

    /**
     * 微信连接认证
     *
     * @param appToken 接入App令牌
     * @param params   请求数据
     * @return 响应数据
     */
    @Nonnull
    protected String connectAuthen(@Nonnull final String appToken, @Nonnull final MultiValueMap<String, String> params) {
        log.debug("connectAuthen(appToken: {},params: {})...", appToken, params);
        //微信加密签名
        final String signature = params.getFirst("signature");
        Assert.hasText(signature, "'signature'不能为空!");
        //时间戳
        final String timestampVal = params.getFirst("timestamp");
        Assert.hasText(timestampVal, "'timestamp'不能为空!");
        final Long timestamp = Strings.isNullOrEmpty(timestampVal) ? null : Long.parseLong(timestampVal);
        //随机数
        final String nonce = params.getFirst("nonce");
        Assert.hasText(nonce, "'nonce'不能为空!");
        //随机字符串
        final String echostr = params.getFirst("echostr");
        Assert.hasText(echostr, "'echostr'不能为空!");
        //加密源字符串/排序
        final String[] sources = new String[]{appToken, timestamp + "", nonce};
        Arrays.sort(sources);
        //签名处理
        final String newSign = DigestUtils.sha1Hex(Joiner.on("").join(sources));
        assert !Strings.isNullOrEmpty(echostr);
        return newSign.equalsIgnoreCase(signature) ? echostr : "";
    }

    /**
     * 根据AppID加载接入令牌
     *
     * @param appKey app接入键
     * @return 接入令牌
     */
    protected abstract String getAppToken(@Nullable final String appKey);

    /**
     * 微信回调通讯
     *
     * @param appKey  app接入键
     * @param reqBody 请求报文体
     * @return 响应报文体
     */
    protected abstract String connectCallback(@Nullable final String appKey, @Nonnull final String reqBody);
}
