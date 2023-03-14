package top.zenyoung.file;

import com.google.common.collect.Maps;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 文件配置
 *
 * @author young
 */
@Data
public class FileProperties implements Serializable {
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
    /**
     * 上传文件后缀处理
     */
    private Map<String, Map<String, String>> extHeaders = Maps.newLinkedHashMap();
}
