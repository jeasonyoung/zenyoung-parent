package top.zenyoung.codec.client;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.codec.client.vo.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 转码上传-客户端接口默认实现
 *
 * @author young
 */
@Slf4j
public class CodecUploadClientDefault extends BaseCodecClientDefault implements CodecUploadClient {
    private static final String BUCKET_REGISTER_URL = "/api/upload/bucket/register";
    private static final String UPLOAD_AUTHORIZE_URL = "/api/upload/authorize";
    private static final String MANUAL_CODEC_URL = "/api/upload/codec/run";
    private static final String CDN_SAFETY_URL = "/api/upload/cdn";

    private static final Executor POOLS;

    static {
        final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("codec-callback-pool-%d").build();
        POOLS = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    /**
     * 构造函数
     *
     * @param host    服务器地址
     * @param account 接入账号
     * @param passwd  接入秘钥
     */
    private CodecUploadClientDefault(@Nonnull final String host, @Nonnull final String account, @Nonnull final String passwd) {
        super(host, account, passwd);
    }

    /**
     * 获取客户端实例
     *
     * @param host    服务器地址
     * @param account 接入账号
     * @param passwd  接入秘钥
     * @return 客户端实例
     */
    public static CodecUploadClientDefault getInstance(@Nonnull final String host, @Nonnull final String account, @Nonnull final String passwd) {
        return new CodecUploadClientDefault(host, account, passwd);
    }

    @Nonnull
    @Override
    public String bucketRegister(@Nonnull final String abbr, @Nonnull final String path) {
        log.debug("bucketRegister(abbr: {},path: {})...", abbr, path);
        Assert.hasText(abbr, "'abbr'不能为空!");
        Assert.hasText(path, "'path'不能为空!");
        final BucketRegisterReq req = new BucketRegisterReq();
        //存储简称
        req.setAbbr(abbr);
        //起始根路径
        req.setPath(path);
        //数据请求处理
        final BucketRegisterResp resp = post(BUCKET_REGISTER_URL, req, BucketRegisterResp.class);
        //返回数据
        return resp.getData();
    }

    @Nonnull
    @Override
    public UploadAuthorize createAuthorize(
            @Nonnull final String bucket,
            @Nonnull final String bizId,
            @Nonnull final String dir,
            @Nullable final String alias,
            @Nullable final String... codecs
    ) {
        log.debug("createAuthorize(bucket: {},bizId: {},dir: {})...", bucket, bizId, dir);
        Assert.hasText(bucket, "'bucket'不能为空!");
        Assert.hasText(bizId, "'bizId'不能为空!");
        Assert.hasText(dir, "'dir'不能为空!");
        final UploadAuthorizeReq req = new UploadAuthorizeReq();
        //存储简称或存储ID
        req.setBucket(bucket);
        //上传业务ID
        req.setBizId(bizId);
        //上传目录
        req.setDir(dir);
        //文件别名[文件中文名,可空]
        req.setAlias(alias);
        //转码代码/简称[可空]
        if (codecs != null) {
            req.setCodecs(Lists.newArrayList(codecs));
        }
        //数据请求处理
        final UploadAuthorizeResp resp = post(UPLOAD_AUTHORIZE_URL, req, UploadAuthorizeResp.class);
        //返回数据
        return resp.getData();
    }

    @Override
    public void triggerManualCodec(@Nonnull final String id) {
        log.debug("triggerManualCodec(id: {})...", id);
        Assert.hasText(id, "'id'不能为空!");
        final ManualCodecReq req = new ManualCodecReq();
        //上传ID
        req.setId(id);
        //数据请求处理
        final ManualCodecResp resp = post(MANUAL_CODEC_URL, req, ManualCodecResp.class);
        log.info("triggerManualCodec=> {}", resp);
    }

    @Nonnull
    @Override
    public CallbackResutResp callbackHandler(@Nonnull final CallbackResutReq req, @Nonnull final Consumer<CallbackResut> callbackBizHandler) {
        log.debug("callbackHandler(req: {},callbackBizHandler: {})...", req, callbackBizHandler);
        //回调参数检查
        Assert.hasText(req.getAccount(), "'req.account'不能为空!");
        Assert.hasText(req.getSign(), "'req.sign'不能为空!");
        Assert.hasText(req.getId(), "'req.id'不能为空!");
        Assert.hasText(req.getBizId(), "'req.bizId'不能为空!");
        //验证签名
        final String oldSign = req.getSign();
        //验算签名
        final String newSign = buildReqSign(req);
        if (!newSign.equalsIgnoreCase(oldSign)) {
            log.warn("callbackHandler(req:{})[old: {},new: {}]-验证签名失败!", req, oldSign, newSign);
            return CallbackResutResp.buildFail("验证签名失败!");
        }
        //业务处理
        POOLS.execute(() -> callbackBizHandler.accept(req));
        //返回消息
        return CallbackResutResp.buildSuccess();
    }

    @Nonnull
    @Override
    public String getCdnSafetyUrl(@Nonnull final String id, @Nonnull final String url, @Nullable final Long expire) {
        log.debug("getCdnSafetyUrl(id: {},url: {},expire: {})...", id, url, expire);
        Assert.hasText(id, "'id'不能为空!");
        Assert.hasText(url, "'url'不能为空!");
        //请求参数
        final CdnSafetyReq req = new CdnSafetyReq();
        //上传ID
        req.setId(id);
        //文件URL
        req.setUrl(url);
        //CDN防盗链有效期(秒, 默认:1800)
        req.setExpire(expire);
        //数据请求处理
        final CdnSafetyResp resp = post(CDN_SAFETY_URL, req, CdnSafetyResp.class);
        final CdnSafetyResp.CdnSafety data = resp.getData();
        return data.getCdnSafetyUrl();
    }
}
