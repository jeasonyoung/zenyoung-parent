package top.zenyoung.codec.client;

import top.zenyoung.codec.client.vo.CallbackResut;
import top.zenyoung.codec.client.vo.CallbackResutReq;
import top.zenyoung.codec.client.vo.CallbackResutResp;
import top.zenyoung.codec.client.vo.UploadAuthorize;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * 转码上传-客户端接口
 *
 * @author young
 */
public interface CodecUploadClient {
    /**
     * 注册存储位置
     *
     * @param abbr 存储简称
     * @param path 起始路径
     * @return 存储ID
     */
    @Nonnull
    String bucketRegister(@Nonnull final String abbr, @Nonnull final String path);

    /**
     * 客户端直传授权签名
     *
     * @param bucket 存储简称或存储ID
     * @param bizId  上传业务ID
     * @param dir    上传目录
     * @param alias  文件别名[文件中文名,可空]
     * @param codecs 转码代码/简称[可空]
     * @return 响应数据
     */
    @Nonnull
    UploadAuthorize createAuthorize(
            @Nonnull final String bucket,
            @Nonnull final String bizId,
            @Nonnull final String dir,
            @Nullable final String alias,
            @Nullable final String... codecs
    );

    /**
     * 触发手动转码
     *
     * @param id 上传ID
     */
    void triggerManualCodec(@Nonnull final String id);

    /**
     * 服务器回调处理
     *
     * @param req                请求数据
     * @param callbackBizHandler 回调业务处理
     * @return 响应数据
     */
    @Nonnull
    CallbackResutResp callbackHandler(@Nonnull final CallbackResutReq req, @Nonnull final Consumer<CallbackResut> callbackBizHandler);

    /**
     * 获取CDN防盗链URL
     *
     * @param id     上传ID
     * @param url    文件URL
     * @param expire CDN防盗链有效期(秒, 默认:1800)
     * @return CDN防盗链安全地址
     */
    @Nonnull
    String getCdnSafetyUrl(@Nonnull final String id, @Nonnull final String url, @Nullable final Long expire);
}
