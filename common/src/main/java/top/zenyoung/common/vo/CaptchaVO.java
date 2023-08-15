package top.zenyoung.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * 验证码VO
 *
 * @author young
 */
@Data
@ApiModel("验证码VO")
@RequiredArgsConstructor(staticName = "of")
public class CaptchaVO implements Serializable {
    /**
     * 验证码ID
     */
    @ApiModelProperty("验证码ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private final Long captchaId;
    /**
     * 验证码图片(base64)
     */
    @ApiModelProperty("验证码图片(base64)")
    private final String base64Data;
}
