package top.zenyoung.wechat.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 微信用户信息
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/13 10:35 下午
 **/
@Data
public class UserInfo implements Serializable {
    /**
     * 用户标识
     */
    @JsonProperty("openid")
    private String openId;
    /**
     * 用户昵称
     */
    @JsonProperty("nickname")
    private String nickName;
    /**
     * 性别(0:未知,1:男,2:女)
     */
    private String sex;
    /**
     * 所属省
     */
    private String province;
    /**
     * 所属城市
     */
    private String city;
    /**
     * 所属国家
     */
    private String country;
    /**
     * 用户头像
     */
    @JsonProperty("headimgurl")
    private String headImgUrl;
    /**
     * 用户特权信息
     */
    private List<String> privilege;
    /**
     * UnionID
     */
    @JsonProperty("unionid")
    private String unionId;
}
