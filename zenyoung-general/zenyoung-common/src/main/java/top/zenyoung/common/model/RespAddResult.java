package top.zenyoung.common.model;

import javax.annotation.Nullable;

/**
 * 响应-新增-结果
 *
 * @author yangyong
 * @version 1.0.3
 **/
public class RespAddResult extends RespResult<AddResult> {

    public static RespAddResult buildFail(@Nullable final String error) {
        final RespAddResult ret = new RespAddResult();
        ret.buildRespFail(error);
        return ret;
    }

    public static RespAddResult buildSuccess(@Nullable final String data) {
        final RespAddResult ret = new RespAddResult();
        ret.buildRespSuccess(new AddResult(data));
        return ret;
    }
}
