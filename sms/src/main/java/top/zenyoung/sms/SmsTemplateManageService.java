package top.zenyoung.sms;

import org.springframework.validation.annotation.Validated;
import top.zenyoung.sms.dto.SmsTemplateAddDTO;
import top.zenyoung.sms.dto.SmsTemplateModifyDTO;
import top.zenyoung.sms.dto.SmsTemplateQueryDTO;
import top.zenyoung.sms.exption.SmsException;
import top.zenyoung.sms.vo.*;

import javax.annotation.Nonnull;

/**
 * 短信模板管理-服务接口
 *
 * @author yangyong
 */
public interface SmsTemplateManageService {

    /**
     * 短信模板-查询
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws SmsException 异常
     */
    SmsTemplateQueryVO query(@Nonnull final SmsTemplateQueryDTO dto) throws SmsException;

    /**
     * 短信模板-申请
     *
     * @param dto 申请数据
     * @return 申请结果
     * @throws SmsException 异常
     */
    SmsTemplateAddVO add(@Validated @Nonnull final SmsTemplateAddDTO dto) throws SmsException;

    /**
     * 短信模板-申请状态
     *
     * @param templateCode 模板Code
     * @return 查询结果
     * @throws SmsException 异常
     */
    SmsTemplateAddStatusVO queryAddStatus(@Nonnull final String templateCode) throws SmsException;

    /**
     * 短信模板-修改
     *
     * @param templateCode 模板Code
     * @param dto          修改数据
     * @return 修改结果
     * @throws SmsException 异常
     */
    SmsTemplateModifyVO modify(@Nonnull final String templateCode, @Validated @Nonnull final SmsTemplateModifyDTO dto) throws SmsException;

    /**
     * 短信模板-删除
     *
     * @param templateCode 模板Code
     * @return 删除结果
     * @throws SmsException 异常
     */
    SmsTemplateDelVO delByTemplateCode(@Nonnull final String templateCode) throws SmsException;
}
