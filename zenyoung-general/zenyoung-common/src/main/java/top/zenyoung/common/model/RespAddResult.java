package top.zenyoung.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

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
     * 构造函数
     *
     * @param base 基类
     */
    protected RespAddResult(@Nonnull final RespResult<RespAddResult.AddResult> base) {
        BeanUtils.copyProperties(base, this);
    }

    /**
     * 静态构建响应新增结果
     *
     * @param code 响应代码
     * @param msg  响应消息
     * @param id   新增主键ID
     * @return 响应新增结果
     */
    public static RespAddResult of(@Nullable final Integer code, @Nullable final String msg, @Nullable final String id) {
        return new RespAddResult(RespResult.of(code, msg, AddResult.of(id)));
    }

    /**
     * 静态构建响应新增结果
     *
     * @param resultCode 响应结果枚举
     * @param msg        响应消息
     * @param id         新增主键ID
     * @return 响应新增结果
     */
    public static RespAddResult of(@Nonnull final ResultCode resultCode, @Nullable final String msg, @Nullable final String id) {
        return new RespAddResult(RespResult.of(resultCode, msg, AddResult.of(id)));
    }

    /**
     * 静态构建响应新增结果
     *
     * @param id 新增主键ID
     * @return 新增结果
     */
    public static RespAddResult ofSuccess(@Nullable final String id) {
        return RespAddResult.of(ResultCode.Success, null, id);
    }

    /**
     * 新增-结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddResult implements Serializable {
        /**
         * 新增主键ID
         */
        private String id;

        /**
         * 静态构建数据
         *
         * @param id 新增主键ID
         * @return 新增结果
         */
        public static AddResult of(@Nullable final String id) {
            return new AddResult(id);
        }
    }
}
