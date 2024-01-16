package top.zenyoung.segment;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * 默认分段ID实现
 *
 * @author young
 */
@Getter
@ToString
public class R2dbcDefaultIdSegment implements R2dbcIdSegment {
    public static final R2dbcDefaultIdSegment OVERFLOW = new R2dbcDefaultIdSegment(SEQUENCE_OVERFLOW, 0, Clock.CACHE.secondTime(), TIME_TO_LIVE_FOREVER);
    private static final AtomicLongFieldUpdater<R2dbcDefaultIdSegment> S = AtomicLongFieldUpdater.newUpdater(R2dbcDefaultIdSegment.class, "sequence");

    private final long maxId;
    private final long offset;
    private final long step;
    private volatile long sequence;
    private final long fetchTime;
    private final long ttl;

    public R2dbcDefaultIdSegment(final long maxId, final long step, final long fetchTime, final long ttl) {
        Preconditions.checkArgument(ttl > 0, "ttl:[%s] must be greater than 0.", ttl);
        this.maxId = maxId;
        this.step = step;
        this.offset = maxId - step;
        this.sequence = offset;
        this.fetchTime = fetchTime;
        this.ttl = ttl;
    }

    @Override
    public Mono<Long> getMaxId() {
        return Mono.just(maxId);
    }

    @Override
    public Mono<Long> incrementAndGet() {
        return isOverflow()
                .flatMap(overflow -> {
                    if (overflow) {
                        return Mono.just(SEQUENCE_OVERFLOW);
                    }
                    final long nextSeq = S.incrementAndGet(this);
                    return isOverflow(nextSeq)
                            .map(isOverflow -> {
                                if (isOverflow) {
                                    return SEQUENCE_OVERFLOW;
                                }
                                return nextSeq;
                            });
                });
    }
}
