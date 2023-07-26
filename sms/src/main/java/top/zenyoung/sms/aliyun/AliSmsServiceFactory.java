package top.zenyoung.sms.aliyun;

import com.alicom.mns.tools.DefaultAlicomMessagePuller;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.sms.*;
import top.zenyoung.sms.config.SmsProperties;
import top.zenyoung.sms.dto.SmsUpCallbackDTO;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * 阿里云-短信服务工厂实现
 *
 * @author yangyong
 */
@Slf4j
@Getter
@RequiredArgsConstructor(staticName = "of")
public class AliSmsServiceFactory extends BaseAliSmsService implements SmsServiceFactory {
    private static final String REGION_ID = "cn-hangzhou";
    private final SmsProperties smsProperties;
    private final List<SmsUpCallbackListener> callbacks;

    private final ObjectMapper objMapper = new ObjectMapper();

    private IAcsClient client;
    private SmsSenderService senderService;
    private SmsSenderStatisticsService senderStatisticsService;
    private SmsSignManageService signManageService;
    private SmsTemplateManageService templateManageService;

    @Override
    public void init() {
        this.client = getAcsClient(smsProperties);
        //发送短信
        this.senderService = AliSmsSenderService.of(this.client);
        //发送统计
        this.senderStatisticsService = AliSmsSenderStatisticsService.of(this.client);
        //签名管理
        this.signManageService = AliSmsSignManageService.of(this.client);
        //模板管理
        this.templateManageService = AliSmsTemplateManageService.of(this.client);
        //扫描回调集合
        log.info("短信上行回调集合=> {}", callbacks);
        //初始化回调处理
        final String callbackQueue;
        if (!Strings.isNullOrEmpty(callbackQueue = smsProperties.getCallbackQueue()) && !CollectionUtils.isEmpty(callbacks)) {
            this.initCallbackHandler(callbackQueue);
        }
    }

    private static IAcsClient getAcsClient(final SmsProperties prop) {
        //设置超时时间-可自行调整
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        //初始化ascClient需要的几个参数
        final String product = "Dysmsapi";
        //短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";
        //短信API产品域名（接口地址固定，无需修改）
        //你的accessKeySecret，参考本文档步骤2
        //初始化ascClient,暂时不支持多region
        final IClientProfile profile = DefaultProfile.getProfile(REGION_ID, prop.getAppKey(), prop.getSecret());
        DefaultProfile.addEndpoint(REGION_ID, product, domain);
        return new DefaultAcsClient(profile);
    }

    private void initCallbackHandler(@Nonnull final String callbackQueue) {
        Assert.hasText(callbackQueue, "'callbackQueue'不能为空");
        final DefaultAlicomMessagePuller puller = new DefaultAlicomMessagePuller();
        //设置异步线程池大小及任务队列的大小，还有无数据线程休眠时间
        puller.setConsumeMinThreadSize(6);
        puller.setConsumeMaxThreadSize(16);
        puller.setThreadQueueSize(200);
        puller.setPullMsgThreadSize(1);
        //和服务端联调问题时开启,平时无需开启，消耗性能
        puller.openDebugLog(smsProperties.isCallbackQueueDebug());
        // 云通信产品下所有的回执消息类型:
        // 短信服务
        // 1:短信回执：SmsReport，
        // 2:短息上行：SmsUp
        // 3:国际短信回执：GlobeSmsReport
        //
        // 号码隐私保护服务
        // 1.呼叫发起时话单报告：SecretStartReport
        // 2.呼叫响铃时报告：SecretRingReport
        // 3.呼叫接听时报告：SecretPickUpReport
        // 4.呼叫结束后话单报告：SecretReport
        // 5.录音状态报告：SecretRecording
        // 6.录音ASR状态报告：SecretAsrReport
        // 7.短信内容报告：SecretSmsIntercept
        // 8.计费通话报告：SecretBillingCallReport
        // 9.计费短信报告：SecretBillingSmsReport
        // 10.异常号码状态推送：SecretExceptionPhoneReport
        // 11.放音录音状态报告：SecretRingRecording
        // 12.电商物流详情报告：SmartLogisticsReport
        // 13.号码管理信息：NumberManagementReport
        //
        // 语音服务
        // 1.呼叫记录消息：VoiceReport
        // 2.呼叫中间状态消息：VoiceCallReport
        // 3.录音记录消息：VoiceRecordReport
        // 4.实时ASR消息：VoiceRTASRReport
        // 5.融合通信呼叫记录消息：ArtcCdrReport
        // 6.融合通信呼叫中间状态：ArtcTempStatusReport
        //启动队列监听
        final String messageType = "SmsUp";
        puller.startReceiveMsg(smsProperties.getAppKey(), smsProperties.getSecret(),
                messageType, callbackQueue, message -> {
                    final String json = message.getMessageBodyAsString();
                    try {
                        if (!Strings.isNullOrEmpty(json)) {
                            final JavaType javaType = objMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
                            final Map<String, Object> data = objMapper.readValue(json, javaType);
                            if (!CollectionUtils.isEmpty(data)) {
                                final SmsUpCallbackDTO dto = parseSmsUpCallback(data);
                                if (Objects.nonNull(dto)) {
                                    callbacks.parallelStream()
                                            .filter(Objects::nonNull)
                                            .forEach(callback -> callback.receiveHandler(dto));
                                }
                            }
                        }
                    } catch (JacksonException e) {
                        log.warn("startReceiveMsg(json: {})-exp: {}", json, e.getMessage());
                        return false;
                    }
                    //消息处理成功，返回true, SDK将调用MNS的delete方法将消息从队列中删除掉
                    return true;
                }
        );
        log.info("initCallbackHandler(callbackQueue: {})[回调加载完成]=> {}", callbackQueue, callbacks);
    }

    private SmsUpCallbackDTO parseSmsUpCallback(@Nonnull final Map<String, Object> data) {
        final UnaryOperator<String> keyValHandler = key -> {
            if (!Strings.isNullOrEmpty(key)) {
                final Object val = data.getOrDefault(key, null);
                if (Objects.nonNull(val)) {
                    if (val instanceof String) {
                        return (String) val;
                    }
                    return val.toString();
                }
            }
            return null;
        };
        //短信扩展号码
        final String destCode = keyValHandler.apply("dest_code");
        //短信发送时间
        final String sendTime = keyValHandler.apply("send_time");
        //消息序列ID
        final String sequenceId = keyValHandler.apply("sequence_id");
        //短信接收号码
        final String mobile = keyValHandler.apply("phone_number");
        //短信内容
        final String content = keyValHandler.apply("content");
        //创建
        return SmsUpCallbackDTO.of(destCode, parseTime(sendTime), sequenceId, mobile, content);
    }
}
