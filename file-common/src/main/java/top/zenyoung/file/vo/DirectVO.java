package top.zenyoung.file.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 直传VO
 *
 * @author young
 */
@Data
@Builder
public class DirectVO implements Serializable {
    /**
     * 直传类型(ali,tencet,huawei)
     */
    private String type;
    /**
     * 直传访问ID
     */
    private String accessId;
    /**
     * 直传Host
     */
    private String host;
    /**
     * 直传策略
     */
    private String policy;
    /**
     * 直传签名
     */
    private String signature;
    /**
     * 直传有效期
     */
    private Long expire;
    /**
     * 直传回调
     */
    private String callback;
    /**
     * 直传目录
     */
    private String dir;
}
