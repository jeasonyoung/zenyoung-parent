package top.zenyoung.sms.aliyun;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendStatisticsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendStatisticsResponse;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.sms.SmsSenderStatisticsService;
import top.zenyoung.sms.dto.SmsSendQueryDetailDTO;
import top.zenyoung.sms.dto.SmsSendQueryStatisticDTO;
import top.zenyoung.sms.enums.SmsSendRange;
import top.zenyoung.sms.enums.SmsSendStatus;
import top.zenyoung.sms.enums.SmsTemplateType;
import top.zenyoung.sms.exption.SmsException;
import top.zenyoung.sms.vo.SmsSendQueryDetailItemVO;
import top.zenyoung.sms.vo.SmsSendQueryDetailVO;
import top.zenyoung.sms.vo.SmsSendQueryStatisticItemVO;
import top.zenyoung.sms.vo.SmsSendQueryStatisticVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ObjLongConsumer;
import java.util.stream.Collectors;

/**
 * 阿里云-短信统计服务
 *
 * @author yangyong
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class AliSmsSenderStatisticsService extends BaseAliSmsService implements SmsSenderStatisticsService {
    private final IAcsClient client;

    private static final Map<SmsSendRange, Integer> RANGE_VAL_MAP = Maps.newHashMap();
    private static final Map<SmsTemplateType, Integer> TYPE_VAL_MAP = Maps.newHashMap();
    private static final Map<Integer, SmsSendStatus> STATUS_VAL_MAP = Maps.newHashMap();

    static {
        //国内短信发送记录
        RANGE_VAL_MAP.put(SmsSendRange.INTERNAL, 1);
        //国际/港澳台短信发送记录
        RANGE_VAL_MAP.put(SmsSendRange.INTERNATIONAL, 2);

        //验证码
        TYPE_VAL_MAP.put(SmsTemplateType.CODE, 0);
        //短信通知
        TYPE_VAL_MAP.put(SmsTemplateType.NOTICE, 1);
        //推广短信
        TYPE_VAL_MAP.put(SmsTemplateType.PROMOTION, 2);
        //国际/港澳台消息
        TYPE_VAL_MAP.put(SmsTemplateType.INTERNATIONAL, 3);

        //等待回执
        STATUS_VAL_MAP.put(1, SmsSendStatus.WAIT);
        //发送失败
        STATUS_VAL_MAP.put(2, SmsSendStatus.FAIL);
        //发送成功
        STATUS_VAL_MAP.put(3, SmsSendStatus.SUCCESS);
    }

    @Override
    public SmsSendQueryStatisticVO queryStatistics(@Nonnull final SmsSendQueryStatisticDTO dto) throws SmsException {
        Assert.notNull(dto.getStart(), "'dto.start'不能为空");
        Assert.notNull(dto.getEnd(), "'dto.end'不能为空");
        final AliQuerySendStatisticsRequest req = new AliQuerySendStatisticsRequest();
        //短信发送范围
        req.setIsGlobe(RANGE_VAL_MAP.get(Optional.ofNullable(dto.getRange()).orElse(SmsSendRange.INTERNAL)));
        //开始日期
        req.setStartDate(dayFormat(dto.getStart()));
        //结束日期
        req.setEndDate(dayFormat(dto.getEnd()));
        //当前页码
        req.setPageIndex(Optional.ofNullable(dto.getPageIndex()).orElse(BasePageDTO.DEF_PAGE_INDEX));
        //每页显示的条数
        req.setPageSize(Optional.ofNullable(dto.getPageSize()).orElse(BasePageDTO.DEF_PAGE_SIZE));
        //模板类型
        if (Objects.nonNull(dto.getTemplateType())) {
            req.setTemplateType(TYPE_VAL_MAP.get(dto.getTemplateType()));
        }
        //签名名称
        if (!Strings.isNullOrEmpty(dto.getSign())) {
            req.setSignName(dto.getSign());
        }
        //发送处理
        final SmsSendQueryStatisticVO vo = new SmsSendQueryStatisticVO();
        handler(client, req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            buildStatus(vo);
            //
            //查询结果处理
            final QuerySendStatisticsResponse.Data data = res.getData();
            if (Objects.nonNull(data)) {
                vo.setTotal(data.getTotalSize());
                vo.setList(Optional.ofNullable(data.getTargetList())
                        .map(rows -> rows.stream()
                                .map(row -> {
                                    final SmsSendQueryStatisticItemVO item = new SmsSendQueryStatisticItemVO();
                                    //发送成功的短信条数
                                    item.setTotalCount(row.getTotalCount());
                                    //接收到回执成功的短信条数
                                    item.setRespondedSuccessCount(row.getRespondedSuccessCount());
                                    //接收到回执失败的短信条数
                                    item.setRespondedFailCount(row.getRespondedFailCount());
                                    //未收到回执的短信条数
                                    item.setNoRespondedCount(row.getNoRespondedCount());
                                    //短信发送日期
                                    item.setSendDate(parseDate(row.getSendDate()));
                                    return item;
                                })
                                .collect(Collectors.toList())
                        )
                        .orElse(Lists.newArrayList())
                );
            }
        });
        return vo;
    }

    @Override
    public SmsSendQueryDetailVO queryDetails(@Nonnull final SmsSendQueryDetailDTO dto) throws SmsException {
        Assert.hasText(dto.getMobile(), "'dto.mobile'不能为空");
        Assert.notNull(dto.getSendDate(), "'dto.sendDate'不能为空");
        final QuerySendDetailsRequest req = new QuerySendDetailsRequest();
        //手机号码
        req.setPhoneNumber(dto.getMobile());
        //发送回执ID
        if (!Strings.isNullOrEmpty(dto.getBizId())) {
            req.setBizId(dto.getBizId());
        }
        //短信发送日期
        req.setSendDate(dayFormat(dto.getSendDate()));
        //当前页码
        req.setCurrentPage((long) Optional.ofNullable(dto.getPageIndex()).orElse(BasePageDTO.DEF_PAGE_INDEX));
        //每页记录
        req.setPageSize((long) Optional.ofNullable(dto.getPageSize()).orElse(BasePageDTO.DEF_PAGE_SIZE));
        //发送请求
        final SmsSendQueryDetailVO vo = new SmsSendQueryDetailVO();
        handler(client, req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            buildStatus(vo);
            //
            vo.setTotal(Optional.ofNullable(res.getTotalCount())
                    .filter(val -> !Strings.isNullOrEmpty(val))
                    .map(Long::parseLong)
                    .orElse(0L)
            );
            //短信发送状态
            final ObjLongConsumer<SmsSendQueryDetailItemVO> buildStatusHandler = (itemVo, status) -> {
                if (Objects.nonNull(itemVo)) {
                    Optional.ofNullable(STATUS_VAL_MAP.getOrDefault((int) status, null))
                            .ifPresent(itemVo::setStatus);
                }
            };
            //查询结果处理
            vo.setList(Optional.ofNullable(res.getSmsSendDetailDTOs())
                    .filter(rows -> !CollectionUtils.isEmpty(rows))
                    .map(rows -> rows.stream()
                            .map(row -> {
                                final SmsSendQueryDetailItemVO item = new SmsSendQueryDetailItemVO();
                                //运营商短信状态码
                                item.setErrCode(row.getErrCode());
                                //短信模板
                                item.setTemplateCode(row.getTemplateCode());
                                //外部流水扩展字段
                                item.setOutId(row.getOutId());
                                //短信接收日期和时间
                                item.setReceiveDate(parseTime(row.getReceiveDate()));
                                //短信发送日期和时间
                                item.setSendDate(parseTime(row.getSendDate()));
                                //接收短信的手机号码
                                item.setMobile(row.getPhoneNum());
                                //短信内容
                                item.setContent(row.getContent());
                                //短信发送状态
                                buildStatusHandler.accept(item, row.getSendStatus());
                                return item;
                            })
                            .collect(Collectors.toList())
                    )
                    .orElse(Lists.newArrayList())
            );
        });
        return vo;
    }

    private static class AliQuerySendStatisticsRequest extends QuerySendStatisticsRequest {
        public AliQuerySendStatisticsRequest() {
            super();
        }

        public void setTemplateType(@Nullable final Integer templateType) {
            if (Objects.nonNull(templateType)) {
                putQueryParameter("TemplateType", templateType);
            }
        }

        public void setSignName(@Nullable final String signName) {
            if (!Strings.isNullOrEmpty(signName)) {
                putQueryParameter("SignName", signName);
            }
        }
    }
}
