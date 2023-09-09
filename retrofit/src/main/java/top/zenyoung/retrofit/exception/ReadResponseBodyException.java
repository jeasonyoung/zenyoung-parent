package top.zenyoung.retrofit.exception;

/**
 * 读取响应报文异常
 *
 * @author young
 */
public class ReadResponseBodyException extends Exception {
    public ReadResponseBodyException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ReadResponseBodyException(final String message) {
        super(message);
    }

    public ReadResponseBodyException(final Throwable cause) {
        super(cause);
    }
}
