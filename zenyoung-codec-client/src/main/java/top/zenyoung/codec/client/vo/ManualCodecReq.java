package top.zenyoung.codec.client.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 手动转码-请求报文
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ManualCodecReq extends BaseCodecReq {
    /**
     * 上传ID
     */
    private String id;

    @Override
    public Map<String, Serializable> toMap() {
        return new HashMap<String, Serializable>(1) {
            {
                //上传ID
                put("id", getId());
            }
        };
    }
}
