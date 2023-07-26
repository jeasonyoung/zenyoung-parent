package top.zenyoung.sms;

import top.zenyoung.sms.dto.SmsSendQueryDetailDTO;
import top.zenyoung.sms.dto.SmsSendQueryStatisticDTO;
import top.zenyoung.sms.exption.SmsException;
import top.zenyoung.sms.vo.SmsSendQueryDetailVO;
import top.zenyoung.sms.vo.SmsSendQueryStatisticVO;

import javax.annotation.Nonnull;

/**
 * 短信统计-服务接口
 *
 * @author yangyong
 */
public interface SmsSenderStatisticsService {

    /**
     * 查询短信发送统计
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws SmsException 异常
     */
    SmsSendQueryStatisticVO queryStatistics(@Nonnull final SmsSendQueryStatisticDTO dto) throws SmsException;

    /**
     * 查询短信发送详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws SmsException 异常
     */
    SmsSendQueryDetailVO queryDetails(@Nonnull final SmsSendQueryDetailDTO dto) throws SmsException;
}