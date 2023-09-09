package top.zenyoung.retrofit.exception;

/**
 * 服务实例选择异常
 *
 * @author young
 */
public class ServiceInstanceChooseException extends RuntimeException {

    public ServiceInstanceChooseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ServiceInstanceChooseException(final String message) {
        super(message);
    }

    public ServiceInstanceChooseException(final Throwable cause) {
        super(cause);
    }
}
