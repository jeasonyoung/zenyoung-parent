package top.zenyoung.codec.client.vo;

import top.zenyoung.web.vo.RespResult;

import java.io.Serializable;

/**
 * 回调结果-响应数据
 *
 * @author young
 */
public class CallbackResutResp extends RespResult<Serializable> {

    public static CallbackResutResp buildSuccess() {
        final CallbackResutResp resp = new CallbackResutResp();
        resp.buildRespSuccess(null);
        return resp;
    }

    public static CallbackResutResp buildFail(final String error) {
        final CallbackResutResp resp = new CallbackResutResp();
        resp.buildRespFail(error);
        return resp;
    }
}
