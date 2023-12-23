package top.zenyoung.segment;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.segment.concurrent.PrefetchWorkerExecutorService;
import top.zenyoung.segment.distributor.IdSegmentDistributor;
import top.zenyoung.segment.distributor.JdbcIdSegmentDistributor;
import top.zenyoung.segment.exception.NotFoundMaxIdException;
import top.zenyoung.segment.exception.SegmentException;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static top.zenyoung.segment.IdSegment.TIME_TO_LIVE_FOREVER;

/**
 * JDBC 分段ID工厂
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class JdbcSegmentIdFactory implements SegmentIdFactory {
    private static final Map<String, Object> LOCKS = Maps.newHashMap();
    private static final Map<String, SegmentIdGenerator> GENERATORS = Maps.newConcurrentMap();
    private static final String INSERT_SEGMENT_SQL = "insert into tbl_segment_id(biz_type,max_id,step,delta) value (?,?,?,?)";
    public static final String FETCH_STEP_SQL = "select max_id, step, delta from tbl_segment_id where biz_type = ?";

    private final DataSource dataSource;
    private final PrefetchWorkerExecutorService prefetchWorkerExecutorService;

    @Override
    public SegmentIdGenerator getIdGenerator(@Nonnull final String bizType) {
        Assert.hasText(bizType, "'bizType'不能为空");
        synchronized (LOCKS.computeIfAbsent(bizType, k -> new Object())) {
            try {
                return GENERATORS.computeIfAbsent(bizType, key -> {
                    final SegmentIdDefinition definition = getIdDefinition(key);
                    final IdSegmentDistributor distributor = new JdbcIdSegmentDistributor(bizType, definition.getStep(), dataSource);
                    return new SegmentChainId(TIME_TO_LIVE_FOREVER, definition.getSafeDistance(), distributor, prefetchWorkerExecutorService);
                });
            } finally {
                LOCKS.remove(bizType);
            }
        }
    }

    private SegmentIdDefinition getIdDefinition(@Nonnull final String bizType) {
        try (final Connection conn = dataSource.getConnection()) {
            SegmentIdDefinition definition;
            try (final PreparedStatement fetchStatement = conn.prepareStatement(FETCH_STEP_SQL)) {
                fetchStatement.setString(1, bizType);
                try (final ResultSet resultSet = fetchStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new NotFoundMaxIdException(bizType);
                    }
                    final long maxId = resultSet.getLong(1);
                    final long step = resultSet.getLong(2);
                    final int safeDistance = resultSet.getInt(3);
                    definition = new SegmentIdDefinition(bizType, safeDistance, maxId - step, step);
                }
            }
            return definition;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new SegmentException(e.getMessage(), e);
        }
    }

    @Override
    public void addSegment(@Nonnull final String bizType, final long maxId, final long step, final long delta) {
        Assert.isTrue(maxId > 0, "'maxId'必须大于0.");
        Assert.isTrue(step > 0, "'step'必须大于0.");
        Assert.isTrue(delta > 0, "'delta'必须大于0.");
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(INSERT_SEGMENT_SQL)) {
            //业务类型
            ps.setString(1, bizType);
            //当前最大ID
            ps.setLong(2, maxId);
            //步长
            ps.setLong(3, step);
            //每次增量
            ps.setLong(4, delta);
            //执行插入
            final boolean ret = ps.execute();
            log.info("addSegment(bizType: {},maxId: {},step: {},delta: {})=> {}", bizType, maxId, step, delta, ret);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new SegmentException(e.getMessage(), e);
        }
    }
}
