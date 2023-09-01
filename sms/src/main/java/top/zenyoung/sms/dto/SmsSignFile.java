package top.zenyoung.sms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 签名文件
 *
 * @author yangyong
 */
@Data
public class SmsSignFile implements Serializable {
    /**
     * 文件内容(Base64格式)
     */
    private String base64Content;
    /**
     * 文件后缀名
     */
    private String suffix;
}
