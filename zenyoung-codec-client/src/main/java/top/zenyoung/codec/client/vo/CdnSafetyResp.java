package top.zenyoung.codec.client.vo;

import lombok.Data;
import top.zenyoung.web.vo.RespResult;

import java.io.Serializable;

/**
 * CDN防盗链下载-响应报文
 *
 * @author young
 */
public class CdnSafetyResp extends RespResult<CdnSafetyResp.CdnSafety> {

    @Data
    public static class CdnSafety implements Serializable {
        /**
         * CDN防盗链安全地址
         */
        private String cdnSafetyUrl;
    }
}
