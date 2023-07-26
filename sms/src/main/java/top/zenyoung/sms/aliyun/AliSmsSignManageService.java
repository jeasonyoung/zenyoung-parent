package top.zenyoung.sms.aliyun;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.sms.SmsSignManageService;
import top.zenyoung.sms.dto.SmsSignAddDTO;
import top.zenyoung.sms.dto.SmsSignModifyDTO;
import top.zenyoung.sms.dto.SmsSignQueryDTO;
import top.zenyoung.sms.enums.SmsAuditStatus;
import top.zenyoung.sms.exption.SmsException;
import top.zenyoung.sms.vo.*;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 阿里云-短信签名管理服务
 *
 * @author yangyong
 */
@RequiredArgsConstructor(staticName = "of")
public class AliSmsSignManageService extends BaseAliSmsService implements SmsSignManageService {
    private final IAcsClient client;

    private static final Map<String, SmsAuditStatus> STATUS_VAL_MAP = Maps.newHashMap();

    static {
        STATUS_VAL_MAP.put("AUDIT_STATE_INIT", SmsAuditStatus.INIT);
        STATUS_VAL_MAP.put("AUDIT_STATE_PASS", SmsAuditStatus.PASS);
        STATUS_VAL_MAP.put("AUDIT_STATE_NOT_PASS", SmsAuditStatus.NOT_PASS);
        STATUS_VAL_MAP.put("AUDIT_STATE_CANCEL", SmsAuditStatus.CANCEL);
    }

    @Override
    public SmsSignQueryVO query(@Nonnull final SmsSignQueryDTO dto) throws SmsException {
        final QuerySmsSignListRequest req = new QuerySmsSignListRequest();
        //页码
        req.setPageIndex(Optional.ofNullable(dto.getPageIndex()).orElse(BasePageDTO.DEF_PAGE_INDEX));
        //页数
        req.setPageSize(Optional.ofNullable(dto.getPageSize()).orElse(BasePageDTO.DEF_PAGE_SIZE));
        //发送处理
        final SmsSignQueryVO vo = new SmsSignQueryVO();
        handler(client, req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            buildStatus(vo);
            //审核状态
            final BiConsumer<SmsSignQueryItemVO, String> createStatusHandler = (itemVO, status) -> {
                if (Objects.nonNull(itemVO) && !Strings.isNullOrEmpty(status)) {
                    Optional.ofNullable(STATUS_VAL_MAP.getOrDefault(status, null))
                            .ifPresent(itemVO::setAuditStatus);
                }
            };
            //审核备注
            final BiConsumer<SmsSignQueryItemVO, QuerySmsSignListResponse.QuerySmsSignDTO.Reason> createReasonHandler = (itemVO, reason) -> {
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
                itemVO.setReason(builder.toString());
            };
            //查询结果处理
            vo.setList(Optional.ofNullable(res.getSmsSignList())
                    .map(rows -> rows.stream()
                            .map(row -> {
                                final SmsSignQueryItemVO item = new SmsSignQueryItemVO();
                                //签名
                                item.setSign(row.getSignName());
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
    public SmsSignAddVO add(@Nonnull final SmsSignAddDTO dto) throws SmsException {
        Assert.hasText(dto.getSignName(), "'dto.signName'不能为空");
        Assert.hasText(dto.getRemark(), "'dto.remark'不能为空");
        Assert.notEmpty(dto.getFiles(), "'dto.files'不能为空");
        //新增
        final AddSmsSignRequest req = new AddSmsSignRequest();
        //签名名称
        req.setSignName(dto.getSignName());
        //短信签名申请说明
        req.setRemark(dto.getRemark());
        //签名文件
        req.setSignFileLists(dto.getFiles().stream()
                .map(file -> {
                    final AddSmsSignRequest.SignFileList sign = new AddSmsSignRequest.SignFileList();
                    sign.setFileContents(file.getBase64Content());
                    sign.setFileSuffix(file.getSuffix());
                    return sign;
                })
                .collect(Collectors.toList())
        );
        //发送
        final SmsSignAddVO vo = new SmsSignAddVO();
        handler(client, req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            vo.setSignName(res.getSignName());
            buildStatus(vo);
        });
        return vo;
    }

    @Override
    public SmsSignAddStatusVO queryAddStatus(@Nonnull final String sign) throws SmsException {
        Assert.hasText(sign, "'sign'不能为空");
        final QuerySmsSignRequest req = new QuerySmsSignRequest();
        req.setSignName(sign);
        final SmsSignAddStatusVO vo = new SmsSignAddStatusVO();
        final Map<Integer, SmsAuditStatus> auditStatusMap = Maps.newHashMap();
        auditStatusMap.put(0, SmsAuditStatus.INIT);
        auditStatusMap.put(1, SmsAuditStatus.PASS);
        auditStatusMap.put(2, SmsAuditStatus.NOT_PASS);
        auditStatusMap.put(10, SmsAuditStatus.CANCEL);
        handler(client, req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            vo.setSignName(res.getSignName());
            buildStatus(vo);
            vo.setAuditStatus(auditStatusMap.getOrDefault(res.getSignStatus(), null));
            vo.setCreateDate(parseTime(res.getCreateDate()));
        });
        return vo;
    }

    @Override
    public SmsSignModifyVO modify(@Nonnull final SmsSignModifyDTO dto) throws SmsException {
        Assert.hasText(dto.getSignName(), "'dto.signName'不能为空");
        Assert.hasText(dto.getRemark(), "'dto.remark'不能为空");
        Assert.notEmpty(dto.getFiles(), "'dto.files'不能为空");
        //修改
        final ModifySmsSignRequest req = new ModifySmsSignRequest();
        //签名名称
        req.setSignName(dto.getSignName());
        //短信签名申请说明
        req.setRemark(dto.getRemark());
        //签名文件
        req.setSignFileLists(dto.getFiles().stream()
                .map(file -> {
                    final ModifySmsSignRequest.SignFileList sign = new ModifySmsSignRequest.SignFileList();
                    sign.setFileContents(file.getBase64Content());
                    sign.setFileSuffix(file.getSuffix());
                    return sign;
                })
                .collect(Collectors.toList())
        );
        //发送
        final SmsSignModifyVO vo = new SmsSignModifyVO();
        handler(client, req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            vo.setSignName(res.getSignName());
            buildStatus(vo);
        });
        return vo;
    }

    @Override
    public SmsSignDelVO delBySign(@Nonnull final String sign) throws SmsException {
        Assert.hasText(sign, "'sign'不能为空");
        final DeleteSmsSignRequest req = new DeleteSmsSignRequest();
        //签名
        req.setSignName(sign);
        //发送
        final SmsSignDelVO vo = new SmsSignDelVO();
        handler(client, req, res -> {
            vo.setCode(res.getCode());
            vo.setMsg(res.getMessage());
            vo.setRequestId(res.getRequestId());
            vo.setSignName(res.getSignName());
            buildStatus(vo);
        });
        return vo;
    }
}
