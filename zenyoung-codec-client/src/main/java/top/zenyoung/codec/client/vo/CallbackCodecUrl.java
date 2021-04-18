package top.zenyoung.codec.client.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zenyoung.common.model.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 回调转码URL
 *
 * @author young
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallbackCodecUrl implements Model {
    /**
     * 访问URL
     */
    private String url;
    /**
     * 转码代码[可空]
     */
    private String codec;
    /**
     * 文件大小(字节)
     */
    private Long size;
    /**
     * 播放时长(毫秒)[可空]
     */
    private Long duration;

    @Override
    public Map<String, Serializable> toMap() {
        return new HashMap<>(4) {
            {
                //访问URL
                put("url", getUrl());
                //转码代码[可空]
                put("codec", getCodec());
                //文件大小(字节)
                put("size", getSize());
                //播放时长(毫秒)[可空]
                put("duration", getDuration());
            }
        };
    }
}
