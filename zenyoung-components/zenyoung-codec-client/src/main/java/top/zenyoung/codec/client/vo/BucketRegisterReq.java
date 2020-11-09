package top.zenyoung.codec.client.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 注册存储位置-请求报文
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BucketRegisterReq extends BaseCodecReq {
    /**
     * 存储名称(小写字母或数字或组合)[可空]
     * 用于在bucket名称,为空时为系统配置默认Bucket
     */
    private String name;
    /**
     * 存储简称(小写字母或数字或组合)
     * 用于接口调用
     */
    private String abbr;
    /**
     * 起始根路径(非'/'字符打头)
     */
    private String path;
    /**
     * 外部访问地址[可空]
     */
    private String url;

    @Override
    public Map<String, Serializable> toMap() {
        return new HashMap<String, Serializable>(4) {
            {
                //存储名称
                put("name", getName());
                //存储简称
                put("abbr", getAbbr());
                //起始根路径
                put("path", getPath());
                //外部访问地址
                put("url", getUrl());
            }
        };
    }
}
