package top.zenyoung.common.model;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 响应-修改-结果
 *
 * @author yangyong
 * @version 1.0.4
 **/
public class RespModifyResult extends RespResult<Serializable> {

    public static RespModifyResult buildFinish() {
        final RespModifyResult ret = new RespModifyResult();
        ret.buildRespSuccess(null);
        return ret;
    }

    public static RespModifyResult buildError(@Nullable final String err) {
        final RespModifyResult ret = new RespModifyResult();
        ret.buildRespFail(err);
        return ret;
    }
}
