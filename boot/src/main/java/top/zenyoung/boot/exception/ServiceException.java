package top.zenyoung.boot.exception;

import top.zenyoung.common.exception.BaseException;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 服务端异常
 *
 * @author young
 */
public class ServiceException extends BaseException {
    /**
     * 构造函数
     *
     * @param msg 异常消息
     */
    public ServiceException(@Nullable final String msg) {
        super(500, msg);
    }

    public ServiceException(@Nonnull final EnumValue enumValue) {
        super(enumValue);
    }
}