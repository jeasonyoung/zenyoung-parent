package top.zenyoung.segment.distributor;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import top.zenyoung.segment.exception.SegmentException;
import top.zenyoung.segment.exception.SegmentNameMissingException;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * R2dbc 分布式分段ID实现
 *
 * @author young
 */
@Slf4j
public class R2dbcIdSegmentDistributor implements R2dbcSegmentDistributor {
    public static final Integer MAX_STEP = 5000;
    public static final String INCREMENT_MAX_ID_SQL = "update tbl_segment_id set max_id=(max_id + :{step}), " +
            "step = :{step}, version=unix_timestamp() where biz_type = :{bizType}";
    public static final String FETCH_MAX_ID_SQL = "select max_id from tbl_segment_id where biz_type = :{bizType}";

    private final String namespace;
    private final long step;
    private final DatabaseClient dataSource;
    private final String incrementMaxIdSql;
    private final String fetchMaxIdSql;

    public R2dbcIdSegmentDistributor(@Nonnull final String namespace, final long step, @Nonnull final DatabaseClient dataSource) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(namespace), "namespace can not be empty!");
        Preconditions.checkArgument(step > 0, "step:[%s] must be greater than 0!", step);
        Preconditions.checkNotNull(dataSource, "dataSource can not be null!");

        this.namespace = namespace;
        this.step = step;
        this.incrementMaxIdSql = INCREMENT_MAX_ID_SQL;
        this.fetchMaxIdSql = FETCH_MAX_ID_SQL;
        this.dataSource = dataSource;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public Mono<Long> nextMaxId(final long step) {
        final long sep = Math.min(step, MAX_STEP);
        final Supplier<Mono<Long>> getMaxHandler = () -> dataSource.sql(incrementMaxIdSql)
                .bind("step", step)
                .bind("bizType", getNamespace())
                .fetch().rowsUpdated().flatMap(affected -> {
                    if (affected == 0) {
                        return Mono.error(new SegmentNameMissingException(getNamespace()));
                    }
                    return dataSource.sql(fetchMaxIdSql)
                            .bind("bizType", getNamespace())
                            .map(row -> {
                                final Long val = row.get("max_id", Long.class);
                                if (Objects.nonNull(val)) {
                                    return val;
                                }
                                return 0L;
                            }).
                            first();
                }).doOnError(e -> {
                    log.error(e.getMessage(), e);
                    throw new SegmentException(e.getMessage(), e);
                });
        return ensureStep(sep)
                .then(getMaxHandler.get());
    }
}
