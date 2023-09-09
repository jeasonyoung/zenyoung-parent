package top.zenyoung.retrofit.exception;

/**
 * Retrofit IO异常
 *
 * @author young
 */
public class RetrofitIOException extends RetrofitException {

    public RetrofitIOException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RetrofitIOException(final String message) {
        super(message);
    }
}
