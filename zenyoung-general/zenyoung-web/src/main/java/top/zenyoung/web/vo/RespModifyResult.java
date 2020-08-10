package top.zenyoung.web.vo;

import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 响应-修改-结果
 *
 * @author yangyong
 * @version 1.0.4
 **/
public class RespModifyResult extends RespResult<Serializable> {

    /**
     * 构造函数
     *
     * @param base 基类
     */
    protected RespModifyResult(@Nonnull final RespResult<Serializable> base) {
        BeanUtils.copyProperties(base, this);
    }

    /**
     * 静态构建响应修改结果
     *
     * @param code 响应代码
     * @param msg  响应消息
     * @return 响应修改结果
     */
    public static RespModifyResult of(@Nullable final Integer code, @Nullable final String msg) {
        return new RespModifyResult(RespResult.of(code, msg, null));
    }

    /**
     * 静态构建响应修改结果
     *
     * @param resultCode 响应结果枚举
     * @param msg        响应消息
     * @return 响应修改结果
     */
    public static RespModifyResult of(@Nonnull final ResultCode resultCode, @Nullable final String msg) {
        return new RespModifyResult(RespResult.of(resultCode, msg, null));
    }

    /**
     * 静态构建响应修改结果
     *
     * @return 响应修改结果
     */
    public static RespModifyResult ofFinish() {
        return RespModifyResult.of(ResultCode.Success, null);
    }
}
