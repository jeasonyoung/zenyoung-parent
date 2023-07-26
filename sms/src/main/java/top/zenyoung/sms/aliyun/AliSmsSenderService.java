package top.zenyoung.sms.aliyun;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.http.MethodType;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.sms.SmsSenderService;
import top.zenyoung.sms.dto.SmsSenderDTO;
import top.zenyoung.sms.exption.SmsException;
import top.zenyoung.sms.vo.SmsSenderVO;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 阿里云-短信发送服务
 *
 * @author yangyong
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class AliSmsSenderService extends BaseAliSmsService implements SmsSenderService {
    private final IAcsClient client;

    @Override
    public SmsSenderVO sender(@Nonnull final SmsSenderDTO dto) throws SmsException {
        Assert.hasText(dto.getMobile(), "'mobile'不能为空");
        Assert.hasText(dto.getTemplate(), "'template'不能为空");
        Assert.notEmpty(dto.getData(), "'data'不能为空");
        //组装请求对象
        final SendSmsRequest req = new SendSmsRequest();
        //使用post提交
        req.setSysMethod(MethodType.POST);
        //必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,
        // 批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
        req.setPhoneNumbers(dto.getMobile());
        //必填:短信签名-可在短信控制台中找到
        req.setSignName(Optional.ofNullable(dto.getSignName())
                .filter(name -> !Strings.isNullOrEmpty(name))
                .orElseThrow(() -> new SmsException("'signName'不能为空"))
        );
        //必填:短信模板-可在短信控制台中找到
        req.setTemplateCode(dto.getTemplate());
        //必填:短信参数
        req.setTemplateParam(toJson(dto.getData()));
        //可选:上行短信扩展码(无特殊需求用户请忽略此字段)
        Optional.ofNullable(dto.getSmsUpExtendCode())
                .filter(code -> !Strings.isNullOrEmpty(code))
                .ifPresent(req::setSmsUpExtendCode);
        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        Optional.ofNullable(dto.getOutId())
                .filter(outId -> !Strings.isNullOrEmpty(outId))
                .ifPresent(req::setOutId);
        //发送请求处理
        final SmsSenderVO vo = new SmsSenderVO();
        handler(client, req, res -> {
            vo.setCode(res.getCode());
            vo.setBizId(res.getBizId());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            buildStatus(vo);
        });
        return vo;
    }

    private static <T> String toJsonHandler(@Nonnull final Collection<T> items) {
        return toJson(items);
    }

    private static <T, R> String toJsonHandler(@Nonnull final Collection<T> items, @Nonnull final Function<T, R> splitHandler) {
        return toJsonHandler(items.stream()
                .map(splitHandler)
                .collect(Collectors.toList())
        );
    }

    @Override
    public SmsSenderVO sender(@Nonnull final List<SmsSenderDTO> dtos) throws SmsException {
        Assert.notEmpty(dtos, "'dtos'不能为空");
        final Function<Function<SmsSenderDTO, String>, List<String>> dtoSplitHandler = split -> dtos.stream()
                .map(split).filter(val -> !Strings.isNullOrEmpty(val)).distinct().collect(Collectors.toList());
        final Function<List<String>, String> joinerHandller = items -> Joiner.on(",").skipNulls().join(items);
        //检查模板
        final List<String> templates = dtoSplitHandler.apply(SmsSenderDTO::getTemplate);
        Assert.notEmpty(templates, "'dto.template'不能为空");
        if (templates.size() > 1) {
            throw new SmsException("批量处理时只能一次发送同一批模板的短信:" + joinerHandller.apply(templates));
        }
        //回执id
        final List<String> outIds = dtoSplitHandler.apply(SmsSenderDTO::getOutId);
        if (!CollectionUtils.isEmpty(outIds) && outIds.size() > 1) {
            throw new SmsException("批量处理时只能一次有一个回执ID:" + joinerHandller.apply(outIds));
        }
        //手机号码拆分处理
        final Map<String, SmsSenderDTO> mobileMap = dtos.stream()
                .filter(dto -> !Strings.isNullOrEmpty(dto.getMobile()) && !Strings.isNullOrEmpty(dto.getTemplate()))
                .map(dto -> {
                    //检查参数集合
                    if (CollectionUtils.isEmpty(dto.getData())) {
                        dto.setData(Maps.newHashMap());
                    }
                    //手机号码
                    final List<String> mobiles = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(dto.getMobile());
                    return mobiles.stream()
                            .filter(mobile -> !Strings.isNullOrEmpty(mobile))
                            .distinct()
                            .map(mobile -> Pair.of(mobile, dto))
                            .collect(Collectors.toList());
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (o, n) -> n));
        //组装参数
        final AliSendBatchSmsRequest req = new AliSendBatchSmsRequest();
        //接收短信的手机号码
        req.setPhoneNumberJson(toJsonHandler(mobileMap.keySet()));
        //短信签名名称
        req.setSignNameJson(toJsonHandler(mobileMap.values(), SmsSenderDTO::getSignName));
        //短信模板
        req.setTemplateCode(templates.get(0));
        //短信模板变量对应的实际值
        req.setTemplateParamJson(toJsonHandler(mobileMap.values(), SmsSenderDTO::getData));
        //分解参数
        final Function<Function<SmsSenderDTO, String>, List<String>> splitHandler = split -> mobileMap.values()
                .stream()
                .map(split)
                .filter(val -> !Strings.isNullOrEmpty(val))
                .collect(Collectors.toList());
        //上行短信扩展码
        final List<String> upCodes = splitHandler.apply(SmsSenderDTO::getSmsUpExtendCode);
        if (!CollectionUtils.isEmpty(upCodes)) {
            req.setSmsUpExtendCodeJson(toJsonHandler(upCodes));
        }
        //外部流水扩展字段
        if (!CollectionUtils.isEmpty(outIds)) {
            req.setOutId(outIds.get(0));
        }
        final SmsSenderVO vo = new SmsSenderVO();
        handler(client, req, res -> {
            vo.setCode(res.getCode());
            vo.setBizId(res.getBizId());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            buildStatus(vo);
        });
        return vo;
    }

    private static class AliSendBatchSmsRequest extends SendBatchSmsRequest {
        public AliSendBatchSmsRequest() {
            super();
        }

        public void setOutId(final String outId) {
            if (!Strings.isNullOrEmpty(outId)) {
                putQueryParameter("OutId", outId);
            }
        }
    }
}
