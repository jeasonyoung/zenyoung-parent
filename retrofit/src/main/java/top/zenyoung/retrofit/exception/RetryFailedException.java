package top.zenyoung.retrofit.exception;

/**
 * 重试失败异常
 *
 * @author young
 */
public class RetryFailedException extends RuntimeException {

    public RetryFailedException(final String message) {
        super(message);
    }

    public RetryFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
