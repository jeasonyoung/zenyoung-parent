package top.zenyoung.codec.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.codec.client.vo.BaseCodecReq;
import top.zenyoung.common.util.SignUtils;
import top.zenyoung.web.vo.RespResult;
import top.zenyoung.webclient.WebClient;
import top.zenyoung.webclient.WebClientUtils;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;

/**
 * 转码客户端实例
 */
@Slf4j
abstract class BaseCodecClientDefault {
    private static final String PATH_SEP = "/";

    private final String host;
    private final String acount;
    private final String passwd;

    private final WebClient client;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     */
    protected BaseCodecClientDefault(@Nonnull final String host, @Nonnull final String account, @Nonnull final String passwd) {
        Assert.hasText(host, "'host'不能为空!");
        Assert.hasText(account, "'account'不能为空!");
        Assert.hasText(passwd, "'passwd'不能为空!");
        //服务器地址
        this.host = host.endsWith(PATH_SEP) ? host.substring(0, host.length() - 1) : host;
        //接入账号
        this.acount = account;
        //接入密码
        this.passwd = passwd;
        //初始化WebClient
        this.client = WebClientUtils.getInstance();
        //
        this.objectMapper = new ObjectMapper();
    }

    @SneakyThrows
    protected <T extends BaseCodecReq, R extends RespResult<?>> R post(@Nonnull final String url, @Nonnull final T req, @Nonnull final Class<R> respClass) {
        log.debug("post(url: {},req: {},respClass: {})...", url, req, respClass);
        //请求地址
        final String postUrl = buildUrl(url);
        //接入账号
        req.setAccount(acount);
        //时间戳
        req.setStamp(System.currentTimeMillis());
        //参数签名处理
        req.setSign(buildReqSign(req));
        //请求处理
        final R resp = client.sendPostJson(postUrl, null, req, objectMapper, respClass);
        if (resp == null) {
            throw new RuntimeException("网络错误!");
        }
        if (resp.getCode() < 0) {
            throw new RuntimeException("[" + resp.getCode() + "]" + resp.getMsg());
        }
        return resp;
    }

    protected String buildUrl(@Nonnull final String url) {
        return host + (url.startsWith(PATH_SEP) ? url : PATH_SEP + url);
    }

    protected <T extends BaseCodecReq> String buildReqSign(@Nonnull final T req) {
        log.debug("buildReqSign(req: {})...", req);
        //检查接入账号
        if(!Strings.isNullOrEmpty(req.getAccount())){
            Assert.isTrue(acount.equalsIgnoreCase(req.getAccount()), "'req.account'错误!");
        }
        final Map<String, Serializable> params = Maps.newLinkedHashMap();
        //检查请求参数
        final Map<String, Serializable> reqMap;
        if (!CollectionUtils.isEmpty(reqMap = req.toMap())) {
            params.putAll(reqMap);
        }
        //时间戳
        params.put("stamp", req.getStamp());
        //签名处理
        return buildSignHandler(params);
    }

    protected String buildSignHandler(@Nonnull final Map<String, Serializable> params) {
        //接入账号
        params.put("account", acount);
        //参数签名处理
        return SignUtils.createSign(params, passwd);
    }

}
