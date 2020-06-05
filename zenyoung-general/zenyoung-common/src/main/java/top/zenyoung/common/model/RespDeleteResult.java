package top.zenyoung.common.model;

import java.io.Serializable;

/**
 * 响应-删除-结果
 *
 * @author yangyong
 * @version 1.0.4
 **/
public class RespDeleteResult extends RespResult<Serializable> {

    public static RespDeleteResult buildFinish() {
        final RespDeleteResult ret = new RespDeleteResult();
        ret.buildRespSuccess(null);
        return ret;
    }
}
