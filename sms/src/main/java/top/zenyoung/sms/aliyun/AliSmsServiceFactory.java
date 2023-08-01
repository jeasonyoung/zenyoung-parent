package top.zenyoung.sms.aliyun;

import com.alicom.mns.tools.DefaultAlicomMessagePuller;
import com.aliyun.mns.model.Message;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.sms.*;
import top.zenyoung.sms.config.SmsProperties;
import top.zenyoung.sms.dto.SmsReportCallbackDTO;
import top.zenyoung.sms.dto.SmsUpCallbackDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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
    private final ObjectMapper objMapper;
    private final List<SmsUpCallbackListener> smsUpCallbacks;
    private final List<SmsReportCallbackListener> smsReportCallbacks;

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
        log.info("短信上行回调集合=> {}", smsUpCallbacks);
        log.info("短信发送回执回调集合=> {}", smsReportCallbacks);
        //初始化回调处理
        final String callbackQueue;
        if (!Strings.isNullOrEmpty(callbackQueue = smsProperties.getCallbackQueue())) {
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
        //上行短信
        final String smsUpMessageType = "SmsUp", smsReportMessageType = "SmsReport";
        if (!CollectionUtils.isEmpty(smsUpCallbacks)) {
            final String queue = buildCallbackQueue(callbackQueue, smsUpMessageType, smsReportMessageType);
            createMessagePullerHandler(queue, smsUpMessageType, SmsUpCallbackDTO.class, smsUpCallbacks);
            log.info("完成[queue: {},type: {}]回调加载初始化: {}", queue, smsUpMessageType, smsUpCallbacks);
        }
        //短信发送报告
        if (!CollectionUtils.isEmpty(smsReportCallbacks)) {
            final String queue = buildCallbackQueue(callbackQueue, smsReportMessageType, smsUpMessageType);
            createMessagePullerHandler(queue, smsReportMessageType, SmsReportCallbackDTO.class, smsReportCallbacks);
            log.info("完成[queue: {},type: {}]回调加载初始化: {}", queue, smsReportMessageType, smsReportCallbacks);
        }
    }

    private String buildCallbackQueue(@Nonnull final String queue, @Nonnull final String messageType, @Nonnull final String... types) {
        final List<String> suffixItems = Lists.newArrayList(types);
        suffixItems.add(messageType);
        final String sep = "-";
        final String prefix = suffixItems.stream()
                .map(s -> {
                    if (queue.endsWith(sep + s)) {
                        final int idx = queue.lastIndexOf(sep + s);
                        return queue.substring(0, idx);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(queue);
        return (prefix.endsWith(sep) ? prefix : prefix + sep) + messageType;
    }

    private <T, K extends CallbackListener<T>> void createMessagePullerHandler(@Nonnull final String callbackQueue,
                                                                               @Nonnull final String messageType,
                                                                               @Nonnull final Class<T> payloadClass,
                                                                               @Nonnull final List<K> callbacks) {
        if (CollectionUtils.isEmpty(callbacks)) {
            log.warn("createMessagePullerHandler: callbacks不能为空");
            return;
        }
        Assert.hasText(callbackQueue, "'callbackQueue'不能为空");
        Assert.hasText(messageType, "'messageType'不能为空");
        final DefaultAlicomMessagePuller puller = new DefaultAlicomMessagePuller();
        //设置异步线程池大小及任务队列的大小，还有无数据线程休眠时间
        puller.setConsumeMinThreadSize(6);
        puller.setConsumeMaxThreadSize(16);
        puller.setThreadQueueSize(200);
        puller.setPullMsgThreadSize(1);
        //和服务端联调问题时开启,平时无需开启，消耗性能
        puller.openDebugLog(smsProperties.isCallbackQueueDebug());
        //回调处理
        final Consumer<T> payloadHandler = payload -> {
            //检查回调处理器
            if (CollectionUtils.isEmpty(callbacks)) {
                return;
            }
            //遍历回调处理器
            callbacks.forEach(callback -> {
                //检查回调是否存在
                if (Objects.nonNull(callback)) {
                    try {
                        //回调处理
                        callback.receiveHandler(payload);
                    } finally {
                        log.info("已完成消息回调处理 [{}]=> {}", callback, payload);
                    }
                }
            });
        };
        //消息接收初始化
        puller.startReceiveMsg(smsProperties.getAppKey(), smsProperties.getSecret(), messageType, callbackQueue,
                message -> parseCallbackPayload(message, payloadClass, payloadHandler)
        );
    }

    private <T> boolean parseCallbackPayload(@Nullable final Message message, @Nonnull final Class<T> payloadClass,
                                             @Nonnull final Consumer<T> handler) {
        log.debug("parseCallback(message: {},payloadClass: {})", message, payloadClass);
        if (Objects.nonNull(message)) {
            final String body = message.getMessageBodyAsString();
            if (!Strings.isNullOrEmpty(body)) {
                try {
                    log.info("parseCallbackPayload[body]=> {}", body);
                    final JavaType javaType = objMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
                    final Map<String, Object> bodyMap = objMapper.readValue(body, javaType);
                    if (!CollectionUtils.isEmpty(bodyMap)) {
                        //解析处理
                        final Function<String, T> parseHandler = val -> {
                            if (!Strings.isNullOrEmpty(val)) {
                                try {
                                    return objMapper.readValue(val, payloadClass);
                                } catch (JacksonException ex) {
                                    log.info("parseCallbackPayload(payloadClass: {})[{}]=> {}", payloadClass, ex.getMessage(), val);
                                }
                            }
                            return null;
                        };
                        //解析处理
                        final T payload = Optional.ofNullable((String) bodyMap.getOrDefault("arg", null))
                                .map(parseHandler)
                                .orElse(parseHandler.apply(body));
                        if (Objects.nonNull(payload)) {
                            handler.accept(payload);
                            //消息处理成功，返回true, SDK将调用MNS的delete方法将消息从队列中删除掉
                            return true;
                        }
                    }
                } catch (JacksonException e) {
                    log.error("parseCallbackPayload-exp: {}  {}", e.getMessage(), body);
                    return true;
                }
            }
        }
        return false;
    }
}
