package top.zenyoung.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * 验证码数据
 *
 * @author young
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor(staticName = "of")
public class CaptchaVO implements Serializable {
    /**
     * 验证码ID
     */
    private final Long captchaId;
    /**
     * 验证码图片(base64)
     */
    private final String base64Data;
}
