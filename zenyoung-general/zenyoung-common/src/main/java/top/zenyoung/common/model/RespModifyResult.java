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

    public RespModifyResult buildSuccess() {
        this.buildRespSuccess(null);
        return this;
    }

    public static RespModifyResult buildFail(@Nullable final String err) {
        final RespModifyResult ret = new RespModifyResult();
        ret.buildRespFail(err);
        return ret;
    }
}
