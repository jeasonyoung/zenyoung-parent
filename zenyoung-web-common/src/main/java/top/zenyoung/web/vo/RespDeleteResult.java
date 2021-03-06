package top.zenyoung.web.vo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 响应-删除-结果
 *
 * @author yangyong
 * @version 1.0.4
 **/
public class RespDeleteResult extends RespResult<Serializable> {

    /**
     * 静态构建响应删除结果
     *
     * @param code 响应代码
     * @param msg  响应消息
     * @return 响应修改结果
     */
    public static RespDeleteResult of(@Nullable final Integer code, @Nullable final String msg) {
        final RespDeleteResult resp = new RespDeleteResult();
        resp.buildResp(code, msg);
        return resp;
    }

    /**
     * 静态构建响应删除结果
     *
     * @param resultCode 响应结果枚举
     * @param msg        响应消息
     * @return 响应修改结果
     */
    public static RespDeleteResult of(@Nonnull final ResultCode resultCode, @Nullable final String msg) {
        final RespDeleteResult resp = new RespDeleteResult();
        resp.buildResp(resultCode, msg);
        return resp;
    }

    /**
     * 静态构建响应删除结果
     *
     * @return 响应修改结果
     */
    public static RespDeleteResult ofFinish() {
        return RespDeleteResult.of(ResultCode.Success, null);
    }
}
