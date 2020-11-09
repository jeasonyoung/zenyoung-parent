package top.zenyoung.codec.client.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 上传认证签名
 *
 * @author young
 */
@Data
public class UploadAuthorize implements Serializable {
    /**
     * 上传ID
     */
    private String id;
    /**
     * 上传地址
     */
    private String host;
    /**
     * 上传目录
     */
    private String dir;
    /**
     * 接入ID
     */
    private String accessId;
    /**
     * 上传策略
     */
    private String policy;
    /**
     * 上传签名
     */
    private String signature;
    /**
     * 过期时间戳,13位到毫秒
     */
    private Long expire;
    /**
     * 回调参数
     */
    private String callback;
}
