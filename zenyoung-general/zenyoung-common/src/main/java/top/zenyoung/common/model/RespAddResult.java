package top.zenyoung.common.model;

import javax.annotation.Nullable;

/**
 * 响应-新增-结果
 *
 * @author yangyong
 * @version 1.0.3
 **/
public class RespAddResult extends RespResult<AddResult> {

    public static RespAddResult buildFinish() {
        return buildFinish(null);
    }

    public static RespAddResult buildFinish(@Nullable final String id) {
        final RespAddResult ret = new RespAddResult();
        ret.buildRespSuccess(new AddResult(id));
        return ret;
    }

    public static RespAddResult buildError(@Nullable final String error) {
        final RespAddResult ret = new RespAddResult();
        ret.buildRespFail(error);
        return ret;
    }
}
