package top.zenyoung.codec.client.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * CDN防盗链下载-请求报文
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CdnSafetyReq extends BaseCodecReq {
    /**
     * 上传ID
     */
    private String id;
    /**
     * 文件URL
     */
    private String url;
    /**
     * CDN防盗链有效期(秒, 默认:1800)
     */
    private Long expire;

    @Override
    public Map<String, Serializable> toMap() {
        return new HashMap<String, Serializable>(3) {
            {
                //上传ID
                put("id", getId());
                //文件URL
                put("url", getUrl());
                //CDN防盗链有效期(秒, 默认:1800)
                put("expire", getExpire());
            }
        };
    }
}
