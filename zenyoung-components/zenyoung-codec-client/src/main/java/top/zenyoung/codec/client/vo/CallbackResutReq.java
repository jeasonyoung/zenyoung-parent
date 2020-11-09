package top.zenyoung.codec.client.vo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.web.vo.EnumData;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 回调结果-请求数据
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CallbackResutReq extends BaseCodecReq implements CallbackResut {
    /**
     * 上传ID
     */
    private String id;
    /**
     * 上传业务ID
     */
    private String bizId;
    /**
     * 文件唯一标识码
     */
    private String uniqueCode;
    /**
     * 状态
     */
    private EnumData status;
    /**
     * 消息
     */
    private String msg;
    /**
     * 转码地址集合
     */
    private List<CallbackCodecUrl> urls;

    @Override
    public Map<String, Serializable> toMap() {
        return new HashMap<String, Serializable>(6) {
            {
                //上传ID
                put("id", getId());
                //接入账号
                put("account", getAccount());
                //上传业务ID
                put("bizId", getBizId());
                //文件唯一标识码
                put("uniqueCode", getUniqueCode());
                //状态
                if (getStatus() != null) {
                    put("状态", Maps.newHashMap(getStatus().toMap()));
                }
                //消息
                put("msg", getMsg());
                //转码地址集合
                if (getUrls() != null) {
                    put("urls", Lists.newArrayList(getUrls().stream()
                            .map(CallbackCodecUrl::toMap)
                            .collect(Collectors.toList()))
                    );
                }
            }
        };
    }
}
