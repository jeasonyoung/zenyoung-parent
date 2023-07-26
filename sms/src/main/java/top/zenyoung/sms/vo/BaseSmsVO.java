package top.zenyoung.sms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 短信VO
 *
 * @author young
 */
@Data
public abstract class BaseSmsVO implements Serializable {
    /**
     * 请求id
     */
    private String requestId;
    /**
     * 状态码
     */
    private String code;
    /**
     * 返回消息
     */
    private String msg;
    /**
     * 状态
     */
    private Boolean status;

    /**
     * 结果是否成功
     *
     * @return 是否成功
     */
    public boolean isSucceed() {
        return status;
    }

    /**
     * 结果是否失败
     *
     * @return 是否失败
     */
    public boolean isFailed() {
        return !status;
    }
}
