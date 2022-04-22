package top.zenyoung.framework.exception;

import top.zenyoung.common.exception.BaseException;

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
    public ServiceException(final String msg) {
        super(500, msg);
    }
}
