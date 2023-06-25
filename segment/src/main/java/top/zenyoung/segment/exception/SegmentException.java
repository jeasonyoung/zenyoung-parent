package top.zenyoung.segment.exception;

/**
 * 分段ID异常
 *
 * @author young
 */
public class SegmentException extends RuntimeException {

    public SegmentException(final String message) {
        super(message);
    }

    public SegmentException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
