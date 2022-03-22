package top.zenyoung.framework.exception;

/**
 * 服务端异常
 *
 * @author young
 */
public class ServiceException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param msg 异常消息
     */
    public ServiceException(final String msg) {
        super(msg);
    }
}
