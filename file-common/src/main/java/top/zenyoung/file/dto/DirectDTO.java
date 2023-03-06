package top.zenyoung.file.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.Duration;

/**
 * 直传DTO
 *
 * @author young
 */
@Data
@Builder
public class DirectDTO implements Serializable {
    /**
     * 直传ID
     */
    private String uploadId;
    /**
     * 接入键
     */
    private String accessKeyId;
    /**
     * 直传文件桶
     */
    private String bucket;
    /**
     * 直传目录
     */
    private String dir;
    /**
     * 直传有效期
     */
    private Duration duration;
    /**
     * 直传地址
     */
    private String host;
    /**
     * 回调URL地址
     */
    private String callbackUrl;
}
