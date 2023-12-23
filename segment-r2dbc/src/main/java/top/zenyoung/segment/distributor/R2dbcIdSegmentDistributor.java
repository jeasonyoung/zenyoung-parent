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
import java.util.Optional;

/**
 * R2dbc 分布式分段ID实现
 *
 * @author young
 */
@Slf4j
public class R2dbcIdSegmentDistributor implements IdSegmentDistributor {
    public static final Integer MAX_STEP = 5000;
    public static final String INCREMENT_MAX_ID_SQL = "update tbl_segment_id set max_id=(max_id + ?), step = ?, version=unix_timestamp() where biz_type = ?";
    public static final String FETCH_MAX_ID_SQL = "select max_id from tbl_segment_id where biz_type = ?";

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
    public long nextMaxId(final long step) {
        final long sep = Math.min(step, MAX_STEP);
        ensureStep(sep);
        try {
            //更新数据
            final Long maxId = dataSource.sql(incrementMaxIdSql)
                    .bind(1, step)
                    .bind(2, step)
                    .bind(3, getNamespace())
                    .fetch()
                    .rowsUpdated()
                    .flatMap(affected -> {
                        if (affected == 0) {
                            return Mono.error(new SegmentNameMissingException(getNamespace()));
                        }
                        return dataSource.sql(fetchMaxIdSql)
                                .bind(1, getNamespace())
                                .fetch()
                                .first()
                                .map(ret -> {
                                    final Object val = ret.getOrDefault("max_id", null);
                                    if (Objects.nonNull(val) && (val instanceof Number)) {
                                        return ((Number) val).longValue();
                                    }
                                    return 0L;
                                });
                    })
                    .block();
            return Optional.ofNullable(maxId).map(Number::longValue).orElse(0L);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SegmentException(e.getMessage(), e);
        }
    }
}
