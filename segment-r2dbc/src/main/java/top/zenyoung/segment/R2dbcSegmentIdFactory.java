package top.zenyoung.segment;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import top.zenyoung.segment.concurrent.PrefetchWorkerExecutorService;
import top.zenyoung.segment.distributor.R2dbcIdSegmentDistributor;
import top.zenyoung.segment.distributor.R2dbcSegmentDistributor;
import top.zenyoung.segment.exception.NotFoundMaxIdException;

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
    public static final String FETCH_STEP_SQL = "select max_id, step, delta from tbl_segment_id where biz_type = :{bizType}";

    private final DatabaseClient dataSource;
    private final PrefetchWorkerExecutorService prefetchWorkerExecutorService;

    @Override
    public Mono<SegmentIdGenerator> getIdGenerator(@Nonnull final String bizType) {
        if (Strings.isNullOrEmpty(bizType)) {
            return Mono.error(new IllegalArgumentException("'bizType'不能为空"));
        }
        final SegmentIdGenerator generator = GENERATORS.getOrDefault(bizType, null);
        if (Objects.nonNull(generator)) {
            return Mono.just(generator);
        }
        return getSegmentIdDefinition(bizType)
                .map(definition -> {
                    final R2dbcSegmentDistributor distributor = new R2dbcIdSegmentDistributor(bizType, definition.getStep(), dataSource);
                    final SegmentIdGenerator data = new R2dbcSegmentChainId(TIME_TO_LIVE_FOREVER,
                            definition.getSafeDistance(), distributor, prefetchWorkerExecutorService);
                    GENERATORS.put(bizType, data);
                    return data;
                });
    }

    private Mono<SegmentIdDefinition> getSegmentIdDefinition(@Nonnull final String bizType) {
        return dataSource.sql(FETCH_STEP_SQL)
                .bind("bizType", bizType)
                .fetch().first()
                .flatMap(row -> {
                    if (Objects.isNull(row)) {
                        return Mono.error(new NotFoundMaxIdException(bizType));
                    }
                    final ToLongFunction<String> nameValHandler = name -> {
                        final Object data = row.getOrDefault(name, null);
                        if (data instanceof Number val) {
                            return val.longValue();
                        }
                        return 0L;
                    };
                    return Mono.fromSupplier(() -> {
                        final long maxId = nameValHandler.applyAsLong("max_id");
                        final long step = nameValHandler.applyAsLong("step");
                        final long delta = nameValHandler.applyAsLong("delta");
                        return SegmentIdDefinition.of(bizType, (int) delta, maxId - step, step);
                    });
                });
    }

    @Override
    public Mono<Boolean> addSegment(@Nonnull final String bizType, final long maxId, final long step, final long delta) {
        if (Strings.isNullOrEmpty(bizType)) {
            return Mono.just(false);
        }
        if (maxId > 0 && step > 0 && delta > 0) {
            return dataSource.sql(INSERT_SEGMENT_SQL)
                    //业务类型
                    .bind(1, bizType)
                    //当前最大ID
                    .bind(2, maxId)
                    //步长
                    .bind(3, step)
                    //每次增量
                    .bind(4, delta)
                    .fetch().rowsUpdated().map(ret -> {
                        log.info("addSegment(bizType: {},maxId: {},step: {},delta: {})=> {}", bizType, maxId, step, delta, ret);
                        return ret > 0;
                    });
        }
        return Mono.just(false);
    }
}
