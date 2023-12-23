package top.zenyoung.segment;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import top.zenyoung.segment.concurrent.PrefetchWorkerExecutorService;
import top.zenyoung.segment.distributor.IdSegmentDistributor;
import top.zenyoung.segment.distributor.R2dbcIdSegmentDistributor;
import top.zenyoung.segment.exception.NotFoundMaxIdException;
import top.zenyoung.segment.exception.SegmentException;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToLongFunction;

import static top.zenyoung.segment.IdSegment.TIME_TO_LIVE_FOREVER;

/**
 * JDBC 分段ID工厂
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class R2dbcSegmentIdFactory implements SegmentIdFactory {
    private static final Map<String, Object> LOCKS = Maps.newHashMap();
    private static final Map<String, SegmentIdGenerator> GENERATORS = Maps.newConcurrentMap();
    private static final String INSERT_SEGMENT_SQL = "insert into tbl_segment_id(biz_type,max_id,step,delta) value (?,?,?,?)";
    public static final String FETCH_STEP_SQL = "select max_id, step, delta from tbl_segment_id where biz_type = ?";

    private final DatabaseClient dataSource;
    private final PrefetchWorkerExecutorService prefetchWorkerExecutorService;

    @Override
    public SegmentIdGenerator getIdGenerator(@Nonnull final String bizType) {
        Assert.hasText(bizType, "'bizType'不能为空");
        synchronized (LOCKS.computeIfAbsent(bizType, k -> new Object())) {
            try {
                return GENERATORS.computeIfAbsent(bizType, key -> {
                    final SegmentIdDefinition definition = getIdDefinition(key);
                    final IdSegmentDistributor distributor = new R2dbcIdSegmentDistributor(bizType, definition.getStep(), dataSource);
                    return new SegmentChainId(TIME_TO_LIVE_FOREVER, definition.getSafeDistance(), distributor, prefetchWorkerExecutorService);
                });
            } finally {
                LOCKS.remove(bizType);
            }
        }
    }

    private SegmentIdDefinition getIdDefinition(@Nonnull final String bizType) {
        try {
            return dataSource.sql(FETCH_STEP_SQL)
                    .bind(1, bizType)
                    .fetch()
                    .first()
                    .flatMap(ret -> {
                        if (Objects.isNull(ret)) {
                            return Mono.error(new NotFoundMaxIdException(bizType));
                        }
                        final ToLongFunction<String> nameValHandler = name -> {
                            final Object val = ret.getOrDefault(name, null);
                            if (Objects.nonNull(val) && (val instanceof Number)) {
                                return ((Number) val).longValue();
                            }
                            return 0L;
                        };
                        return Mono.fromSupplier(() -> {
                            final long maxId = nameValHandler.applyAsLong("max_id");
                            final long step = nameValHandler.applyAsLong("step");
                            final long delta = nameValHandler.applyAsLong("delta");
                            return new SegmentIdDefinition(bizType, (int) delta, maxId - step, step);
                        });
                    })
                    .block();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SegmentException(e.getMessage(), e);
        }
    }

    @Override
    public void addSegment(@Nonnull final String bizType, final long maxId, final long step, final long delta) {
        Assert.isTrue(maxId > 0, "'maxId'必须大于0.");
        Assert.isTrue(step > 0, "'step'必须大于0.");
        Assert.isTrue(delta > 0, "'delta'必须大于0.");
        try {
            dataSource.sql(INSERT_SEGMENT_SQL)
                    //业务类型
                    .bind(1, bizType)
                    //当前最大ID
                    .bind(2, maxId)
                    //步长
                    .bind(3, step)
                    //每次增量
                    .bind(4, delta)
                    .fetch().rowsUpdated()
                    .map(ret -> {
                        log.info("addSegment(bizType: {},maxId: {},step: {},delta: {})=> {}", bizType, maxId, step, delta, ret);
                        return ret;
                    })
                    .block();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SegmentException(e.getMessage(), e);
        }
    }
}
