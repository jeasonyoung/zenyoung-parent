package top.zenyoung.codec.client.vo;

import lombok.Data;
import top.zenyoung.common.model.Model;

import java.io.Serializable;
import java.util.Map;

/**
 * 转码请求基类
 *
 * @author young
 */
@Data
public abstract class BaseCodecReq implements Model {
    /**
     * 接入账号
     */
    private String account;
    /**
     * 时间戳
     */
    private Long stamp;
    /**
     * 参数签名
     */
    private String sign;

    /**
     * 数据模型转换成Map数据
     *
     * @return Map数据
     */
    @Override
    public abstract Map<String, Serializable> toMap();
}
