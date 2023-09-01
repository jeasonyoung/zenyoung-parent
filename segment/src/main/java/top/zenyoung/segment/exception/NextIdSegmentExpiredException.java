package top.zenyoung.segment.exception;

import com.google.common.base.Strings;
import lombok.Getter;
import top.zenyoung.segment.IdSegment;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Next Id分段过期异常
 *
 * @author young
 */
@Getter
public class NextIdSegmentExpiredException extends SegmentException {
    private static final AtomicLong TIMES = new AtomicLong(0);
    private final IdSegment current;
    private final IdSegment next;

    public NextIdSegmentExpiredException(@Nonnull final IdSegment current, @Nonnull final IdSegment next) {
        super(Strings.lenientFormat("The next IdSegment:[%s] cannot be before the current IdSegment:[%s]-- times:[%s].",
                next,
                current,
                TIMES.incrementAndGet())
        );
        this.current = current;
        this.next = next;
    }
}
