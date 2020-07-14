package top.zenyoung.wechat.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * JS-SDK票据
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/12 2:48 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsTicket implements Serializable {
    /**
     * 临时票据
     */
    private String ticket;
    /**
     * 有效期(7200s)
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    public static JsTicket of(@Nonnull final String ticket, @Nonnull final Integer expiresIn) {
        return new JsTicket(ticket, expiresIn);
    }
}
