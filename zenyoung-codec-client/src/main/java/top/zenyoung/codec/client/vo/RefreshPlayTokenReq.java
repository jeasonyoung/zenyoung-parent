package top.zenyoung.codec.client.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 刷新播放令牌-请求数据
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefreshPlayTokenReq extends BaseCodecReq {
    /**
     * 刷新令牌
     */
    private String refreshToken;

    @Override
    public Map<String, Serializable> toMap() {
        return new HashMap<>(1) {
            {
                //刷新令牌
                put("refreshToken", getRefreshToken());
            }
        };
    }
}
