package top.zenyoung.framework.runtime.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zenyoung.framework.service.AuthCaptchaService;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 图形验证码-控制器
 *
 * @author young
 */
@RestController
@Api("图形验证码")
@RequiredArgsConstructor
@RequestMapping("/auth/captcha")
public class AuthCaptchaController extends BaseController {
    private final AuthCaptchaService captchaService;

    /**
     * 获取图形验证码
     *
     * @return 图形验证码数据
     */
    @GetMapping
    public ResultVO<CaptchaVO> getCaptcha() {
        final AuthCaptchaService.AuthCaptcha captcha = captchaService.createCaptcha();
        return success(CaptchaVO.of(captcha.getCaptchaId(), captcha.getBase64Data()));
    }

    /**
     * 图形验证码数据
     */
    @Data
    @ApiModel("图形验证码数据")
    @AllArgsConstructor(staticName = "of")
    private static class CaptchaVO {
        /**
         * 验证码ID
         */
        @ApiModelProperty("验证码ID")
        private Long captchaId;
        /**
         * 验证码图片(base64)
         */
        @ApiModelProperty("验证码图片(base64)")
        private String base64Data;
    }
}
