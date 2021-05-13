package top.zenyoung.web.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 响应-新增-结果
 *
 * @author yangyong
 * @version 1.0.3
 **/
public class RespAddResult extends RespResult<RespAddResult.AddResult> {

    /**
     * 静态构建响应新增结果
     *
     * @param code 响应代码
     * @param msg  响应消息
     * @param id   新增主键ID
     * @return 响应新增结果
     */
    public static RespAddResult of(@Nullable final Integer code, @Nullable final String msg, @Nullable final Serializable id) {
        final RespAddResult resp = new RespAddResult();
        resp.buildResp(code, msg, AddResult.of(id));
        return resp;
    }

    /**
     * 静态构建响应新增结果
     *
     * @param resultCode 响应结果枚举
     * @param msg        响应消息
     * @param id         新增主键ID
     * @return 响应新增结果
     */
    public static RespAddResult of(@Nonnull final ResultCode resultCode, @Nullable final String msg, @Nullable final Serializable id) {
        final RespAddResult resp = new RespAddResult();
        resp.buildResp(resultCode, msg, AddResult.of(id));
        return resp;
    }

    /**
     * 静态构建响应新增结果
     *
     * @param id 新增主键ID
     * @return 新增结果
     */
    public static RespAddResult ofSuccess(@Nullable final Serializable id) {
        return RespAddResult.of(ResultCode.Success, null, id);
    }

    /**
     * 新增-结果
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AddResult implements Serializable {
        /**
         * 新增主键ID
         */
        private final Serializable id;

        public static AddResult of(@Nullable final Serializable id) {
            return new AddResult(id);
        }
    }
}
