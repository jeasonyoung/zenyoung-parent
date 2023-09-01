package top.zenyoung.sms.aliyun;

import com.aliyuncs.dysmsapi.model.v20170525.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.sms.SmsTemplateManageService;
import top.zenyoung.sms.dto.SmsTemplateAddDTO;
import top.zenyoung.sms.dto.SmsTemplateModifyDTO;
import top.zenyoung.sms.dto.SmsTemplateQueryDTO;
import top.zenyoung.sms.enums.SmsAuditStatus;
import top.zenyoung.sms.enums.SmsTemplateType;
import top.zenyoung.sms.exption.SmsException;
import top.zenyoung.sms.vo.*;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

/**
 * 阿里云-短信模板管理服务
 *
 * @author yangyong
 */
@Slf4j
public class AliSmsTemplateManageService extends BaseAliSmsService implements SmsTemplateManageService {
    private static final Map<SmsTemplateType, Integer> TYPE_VAL_MAP = Maps.newHashMap();
    private static final Map<Integer, SmsTemplateType> VAL_TYPE_MAP;
    private static final Map<Integer, SmsAuditStatus> VAL_STATUS_MAP = Maps.newHashMap();
    private static final Map<String, SmsAuditStatus> TXT_STATUS_MAP = Maps.newHashMap();

    static {
        //验证码
        TYPE_VAL_MAP.put(SmsTemplateType.CODE, 0);
        //短信通知
        TYPE_VAL_MAP.put(SmsTemplateType.NOTICE, 1);
        //推广短信
        TYPE_VAL_MAP.put(SmsTemplateType.PROMOTION, 2);
        //国际/港澳台消息
        TYPE_VAL_MAP.put(SmsTemplateType.INTERNATIONAL, 3);
        //反转
        VAL_TYPE_MAP = HashBiMap.create(TYPE_VAL_MAP).inverse();
        //审核中
        VAL_STATUS_MAP.put(0, SmsAuditStatus.INIT);
        TXT_STATUS_MAP.put("AUDIT_STATE_INIT", SmsAuditStatus.INIT);
        //审核通过
        VAL_STATUS_MAP.put(1, SmsAuditStatus.PASS);
        TXT_STATUS_MAP.put("AUDIT_STATE_PASS", SmsAuditStatus.PASS);
        //审核未通过
        VAL_STATUS_MAP.put(2, SmsAuditStatus.NOT_PASS);
        TXT_STATUS_MAP.put("AUDIT_STATE_NOT_PASS", SmsAuditStatus.NOT_PASS);
        //取消审核
        VAL_STATUS_MAP.put(10, SmsAuditStatus.CANCEL);
        TXT_STATUS_MAP.put("AUDIT_STATE_CANCEL", SmsAuditStatus.CANCEL);
        TXT_STATUS_MAP.put("AUDIT_SATE_CANCEL", SmsAuditStatus.CANCEL);
    }

    public AliSmsTemplateManageService(@Nonnull final ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public SmsTemplateQueryVO query(@Nonnull final SmsTemplateQueryDTO dto) throws SmsException {
        //初始化
        final QuerySmsTemplateListRequest req = new QuerySmsTemplateListRequest();
        //页码
        req.setPageIndex(Optional.ofNullable(dto.getPageIndex()).orElse(BasePageDTO.DEF_PAGE_INDEX));
        //页数
        req.setPageSize(Optional.ofNullable(dto.getPageSize()).orElse(BasePageDTO.DEF_PAGE_SIZE));
        //发送处理
        final SmsTemplateQueryVO vo = new SmsTemplateQueryVO();
        handler(req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            buildStatus(vo);
            //模板类型
            final ObjIntConsumer<SmsTemplateQueryItemVO> createTemplateTypeHandler = (itemVo, type) -> {
                if (Objects.nonNull(itemVo)) {
                    Optional.ofNullable(VAL_TYPE_MAP.getOrDefault(type, null))
                            .ifPresent(itemVo::setType);
                }
            };
            //审核状态
            final BiConsumer<SmsTemplateQueryItemVO, String> createStatusHandler = (itemVo, status) -> {
                if (Objects.nonNull(itemVo) && !Strings.isNullOrEmpty(status)) {
                    Optional.ofNullable(TXT_STATUS_MAP.getOrDefault(status, null))
                            .ifPresent(itemVo::setAuditStatus);
                }
            };
            //审核备注
            final BiConsumer<SmsTemplateQueryItemVO, QuerySmsTemplateListResponse.SmsStatsResultDTO.Reason> createReasonHandler = (itemVo, reason) -> {
                if (Objects.nonNull(itemVo)) {
                    final StringBuilder builder = new StringBuilder();
                    if (Objects.nonNull(reason)) {
                        if (!Strings.isNullOrEmpty(reason.getRejectInfo())) {
                            builder.append(reason.getRejectInfo()).append(" ");
                        }
                        if (!Strings.isNullOrEmpty(reason.getRejectSubInfo())) {
                            builder.append(reason.getRejectSubInfo()).append(" ");
                        }
                        if (!Strings.isNullOrEmpty(reason.getRejectDate())) {
                            builder.append(reason.getRejectDate());
                        }
                    }
                    itemVo.setReason(builder.toString());
                }
            };
            //查询结果处理
            vo.setList(Optional.ofNullable(res.getSmsTemplateList())
                    .map(rows -> rows.stream()
                            .map(row -> {
                                final SmsTemplateQueryItemVO item = new SmsTemplateQueryItemVO();
                                //模板名称
                                item.setName(row.getTemplateName());
                                //模板Code
                                item.setTemplateCode(row.getTemplateCode());
                                //模板内容
                                item.setContent(row.getTemplateContent());
                                //模板类型
                                createTemplateTypeHandler.accept(item, row.getTemplateType());
                                //审核状态
                                createStatusHandler.accept(item, row.getAuditStatus());
                                //创建时间
                                item.setCreateDate(parseTime(row.getCreateDate()));
                                //审核备注
                                createReasonHandler.accept(item, row.getReason());
                                return item;
                            })
                            .collect(Collectors.toList())
                    )
                    .orElse(Lists.newArrayList())
            );
        });
        return vo;
    }

    @Override
    public SmsTemplateAddVO add(@Nonnull final SmsTemplateAddDTO dto) throws SmsException {
        Assert.hasText(dto.getName(), "'dto.name'不能为空");
        Assert.notNull(dto.getType(), "'dto.type'不能为空");
        Assert.hasText(dto.getContent(), "'dto.content'不能为空");
        //创建请求
        final AddSmsTemplateRequest req = new AddSmsTemplateRequest();
        //模板名称
        req.setTemplateName(dto.getName());
        //模板类型
        req.setTemplateType(TYPE_VAL_MAP.get(dto.getType()));
        //模板内容
        req.setTemplateContent(dto.getContent());
        //模板说明
        req.setRemark(Optional.ofNullable(dto.getRemark())
                .filter(remark -> !Strings.isNullOrEmpty(remark))
                .orElse(dto.getName())
        );
        final SmsTemplateAddVO vo = new SmsTemplateAddVO();
        //发送处理
        handler(req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            vo.setTemplateCode(res.getTemplateCode());
            buildStatus(vo);
        });
        return vo;
    }

    @Override
    public SmsTemplateAddStatusVO queryAddStatus(@Nonnull final String templateCode) throws SmsException {
        Assert.hasText(templateCode, "'templateCode'不能为空");
        final QuerySmsTemplateRequest req = new QuerySmsTemplateRequest();
        //模板Code
        req.setTemplateCode(templateCode);
        //发送处理
        final SmsTemplateAddStatusVO vo = new SmsTemplateAddStatusVO();
        handler(req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            //
            vo.setTemplateCode(res.getTemplateCode());
            vo.setName(res.getTemplateName());
            vo.setContent(res.getTemplateContent());
            vo.setTemplateStatus(VAL_STATUS_MAP.getOrDefault(res.getTemplateStatus(), null));
            vo.setType(VAL_TYPE_MAP.getOrDefault(res.getTemplateType(), null));
            vo.setCreateDate(parseTime(res.getCreateDate()));
            vo.setReason(res.getReason());
            buildStatus(vo);
            vo.setCreateDate(parseTime(res.getCreateDate()));
        });
        return vo;
    }

    @Override
    public SmsTemplateModifyVO modify(@Nonnull final String templateCode, @Nonnull final SmsTemplateModifyDTO dto) throws SmsException {
        Assert.hasText(templateCode, "'templateCode'不能为空");
        Assert.hasText(dto.getName(), "'dto.name'不能为空");
        Assert.notNull(dto.getType(), "'dto.type'不能为空");
        Assert.hasText(dto.getContent(), "'dto.content'不能为空");
        final ModifySmsTemplateRequest req = new ModifySmsTemplateRequest();
        //模板名称
        req.setTemplateName(dto.getName());
        //模板类型
        req.setTemplateType(TYPE_VAL_MAP.get(dto.getType()));
        //模板内容
        req.setTemplateContent(dto.getContent());
        //模板Code
        req.setTemplateCode(templateCode);
        //发送处理
        final SmsTemplateModifyVO vo = new SmsTemplateModifyVO();
        handler(req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            vo.setTemplateCode(res.getTemplateCode());
            buildStatus(vo);
        });
        return vo;
    }

    @Override
    public SmsTemplateDelVO delByTemplateCode(@Nonnull final String templateCode) throws SmsException {
        Assert.hasText(templateCode, "'templateCode'不能为空");
        final DeleteSmsTemplateRequest req = new DeleteSmsTemplateRequest();
        //模板Code
        req.setTemplateCode(templateCode);
        final SmsTemplateDelVO vo = new SmsTemplateDelVO();
        handler(req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            vo.setTemplateCode(res.getTemplateCode());
            buildStatus(vo);
        });
        return vo;
    }
}
