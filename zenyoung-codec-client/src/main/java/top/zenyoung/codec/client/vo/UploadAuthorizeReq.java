package top.zenyoung.codec.client.vo;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上传授权-请求报文
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UploadAuthorizeReq extends BaseCodecReq {
    /**
     * 上传业务ID
     */
    private String bizId;
    /**
     * 上传目录
     */
    private String dir;
    /**
     * 文件别名[文件中文名,可空]
     */
    private String alias;
    /**
     * 转码代码/简称[可空]
     */
    private List<String> codecs;
    /**
     * 存储简称或存储ID
     */
    private String bucket;

    @Override
    public Map<String, Serializable> toMap() {
        return new HashMap<>(5) {
            {
                //上传业务ID
                put("bizId", getBizId());
                //上传目录
                put("dir", getDir());
                //文件别名[文件中文名,可空]
                put("alias", getAlias());
                //转码代码/简称[可空]
                final List<String> codecItems;
                if (!CollectionUtils.isEmpty(codecItems = getCodecs())) {
                    put("codecs", Lists.newArrayList(codecItems));
                }
                //存储简称或存储ID
                put("bucket", getBucket());
            }
        };
    }
}
