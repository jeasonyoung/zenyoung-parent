package top.zenyoung.codec.client.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 播放令牌-请求数据
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PlayTokenReq extends BaseCodecReq {
    /**
     * 视频ID(上传ID)
     */
    private String vodId;
    /**
     * 播放URL
     */
    private String playUrl;
    /**
     * 播放渠道号(100:PC-WEB,101:iOS,102:Android)
     */
    private Integer channel;

    @Override
    public final Map<String, Serializable> toMap() {
        return new HashMap<String, Serializable>(3) {
            {
                //视频ID(上传ID)
                put("vodId", getVodId());
                //播放URL
                put("playUrl", getPlayUrl());
                //播放渠道号
                put("channel", getChannel());
            }
        };
    }
}
