package top.zenyoung.sms;

import org.springframework.validation.annotation.Validated;
import top.zenyoung.sms.dto.SmsSignAddDTO;
import top.zenyoung.sms.dto.SmsSignModifyDTO;
import top.zenyoung.sms.dto.SmsSignQueryDTO;
import top.zenyoung.sms.exption.SmsException;
import top.zenyoung.sms.vo.*;

import javax.annotation.Nonnull;

/**
 * 短信签名管理-服务接口
 *
 * @author yangyong
 */
public interface SmsSignManageService {

    /**
     * 短信签名-查询
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws SmsException 异常
     */
    SmsSignQueryVO query(@Nonnull final SmsSignQueryDTO dto) throws SmsException;

    /**
     * 短信签名-申请
     *
     * @param dto 申请数据
     * @return 申请结果
     * @throws SmsException 异常
     */
    SmsSignAddVO add(@Validated @Nonnull final SmsSignAddDTO dto) throws SmsException;

    /**
     * 短信签名-申请状态-查询
     *
     * @param sign 短信签名
     * @return 查询结果
     * @throws SmsException 异常
     */
    SmsSignAddStatusVO queryAddStatus(@Nonnull final String sign) throws SmsException;

    /**
     * 短信签名-修改
     *
     * @param dto 修改数据
     * @return 修改结果
     * @throws SmsException 异常
     */
    SmsSignModifyVO modify(@Validated @Nonnull final SmsSignModifyDTO dto) throws SmsException;

    /**
     * 短信签名-删除
     *
     * @param sign 签名
     * @return 删除结果
     * @throws SmsException 异常
     */
    SmsSignDelVO delBySign(@Nonnull final String sign) throws SmsException;
}
