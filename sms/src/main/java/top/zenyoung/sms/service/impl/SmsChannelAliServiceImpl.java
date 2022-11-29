package top.zenyoung.sms.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.boot.exception.ServiceException;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.sms.service.SmsChannelService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 短信通道(阿里云)-服务接口
 *
 * @author young
 */
@Slf4j
public class SmsChannelAliServiceImpl implements SmsChannelService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IAcsClient client;

    /**
     * 构造函数
     *
     * @param appKey 接入账号
     * @param secret 接入秘钥
     */
    public SmsChannelAliServiceImpl(@Nonnull final String appKey, @Nonnull final String secret) {
        //初始化ascClient需要的几个参数
        final String product = "Dysmsapi";
        //短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";
        //短信API产品域名（接口地址固定，无需修改）
        //你的accessKeySecret，参考本文档步骤2
        //初始化ascClient,暂时不支持多region
        final IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", appKey, secret);
        DefaultProfile.addEndpoint("cn-hangzhou", product, domain);
        this.client = new DefaultAcsClient(profile);
    }


    @Override
    public void send(@Nonnull final String templateCode, @Nonnull final Map<String, Object> params,
                     @Nullable final String signName, @Nonnull final String... mobile) {
        try {
            //组装请求对象
            final SendSmsRequest request = new SendSmsRequest();
            //使用post提交
            request.setSysMethod(MethodType.POST);
            //必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
            request.setPhoneNumbers(Stream.of(mobile).filter(m -> !Strings.isNullOrEmpty(m)).collect(Collectors.joining(",")));
            //必填:短信签名-可在短信控制台中找到
            request.setSignName(signName);
            //必填:短信模板-可在短信控制台中找到
            request.setTemplateCode(templateCode);
            //必填:短信参数
            request.setTemplateParam(JsonUtils.toJson(objectMapper, params));
            //请求失败这里会抛ClientException异常
            final SendSmsResponse resp = client.getAcsResponse(request);
            log.info("resp=> {}", resp.getCode());
        } catch (Throwable e) {
            log.error("send(templateCode: {},params: {},signName: {},mobile: {})-exp: {}", templateCode, params, signName, mobile, e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }
}
