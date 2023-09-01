package top.zenyoung.sms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 短信签名-新增DTO
 *
 * @author young
 */
@Data
public class SmsSignAddDTO implements Serializable {
    /**
     * 签名名称
     */
    private String signName;
    /**
     * 短信签名申请说明
     */
    private String remark;
    /**
     * 签名文件集合
     */
    private List<SmsSignFile> files;
}
