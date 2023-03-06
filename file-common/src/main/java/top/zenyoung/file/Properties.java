package top.zenyoung.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件配置
 *
 * @author young
 */
@Data
public class Properties implements Serializable {
    /**
     * 接入点
     */
    private String endpoint;
    /**
     * 接入键
     */
    private String accessKeyId;
    /**
     * 接入秘钥
     */
    private String accessKeySecret;
}
