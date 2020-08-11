package top.zenyoung.web.vo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 响应数据结果
 *
 * @author yangyong
 * @version 1.0
 **/
public class RespDataResult<T extends Serializable> extends RespResult<DataResult<T>> {

    /**
     * 静态构建响应数据结果
     *
     * @param code 响应状态码
     * @param msg  响应消息
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 响应数据结果
     */
    public static <T extends Serializable> RespDataResult<T> of(@Nullable final Integer code, @Nullable final String msg, @Nullable final DataResult<T> data) {
        final RespDataResult<T> resp = new RespDataResult<>();
        resp.buildResp(code, msg, data);
        return resp;
    }

    /**
     * 静态构建响应数据结果
     *
     * @param resultCode 响应结果枚举
     * @param data       响应数据
     * @param <T>        数据类型
     * @return 响应数据结果
     */
    public static <T extends Serializable> RespDataResult<T> of(@Nonnull final ResultCode resultCode, @Nullable final DataResult<T> data) {
        final EnumData ret = EnumData.parse(resultCode);
        return of(ret.getVal(), ret.getTitle(), data);
    }

    /**
     * 静态构建响应数据结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应数据结果
     */
    public static <T extends Serializable> RespDataResult<T> ofSuccess(@Nullable final DataResult<T> data) {
        return RespDataResult.of(ResultCode.Success, data);
    }
}
