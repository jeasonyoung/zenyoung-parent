package top.zenyoung.segment;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * 默认分段ID实现
 *
 * @author young
 */
@Getter
@ToString
public class DefaultIdSegment implements IdSegment {
    private final long maxId;
    private final long offset;
    private final long step;
    private volatile long sequence;
    private final long fetchTime;
    private final long ttl;

    public DefaultIdSegment(final long maxId, final long step) {
        this(maxId, step, System.currentTimeMillis() / 1000, TIME_TO_LIVE_FOREVER);
    }

    public DefaultIdSegment(final long maxId, final long step, final long fetchTime, final long ttl) {
        Preconditions.checkArgument(ttl > 0, "ttl:[%s] must be greater than 0.", ttl);
        this.maxId = maxId;
        this.step = step;
        this.offset = maxId - step;
        this.sequence = offset;
        this.fetchTime = fetchTime;
        this.ttl = ttl;
    }

    public static final DefaultIdSegment OVERFLOW = new DefaultIdSegment(SEQUENCE_OVERFLOW, 0, System.currentTimeMillis() / 1000, TIME_TO_LIVE_FOREVER);

    private static final AtomicLongFieldUpdater<DefaultIdSegment> S = AtomicLongFieldUpdater.newUpdater(DefaultIdSegment.class, "sequence");

    @Override
    public long incrementAndGet() {
        if (isOverflow()) {
            return SEQUENCE_OVERFLOW;
        }
        final long nextSeq = S.incrementAndGet(this);
        if (isOverflow(nextSeq)) {
            return SEQUENCE_OVERFLOW;
        }
        return nextSeq;
    }
}
