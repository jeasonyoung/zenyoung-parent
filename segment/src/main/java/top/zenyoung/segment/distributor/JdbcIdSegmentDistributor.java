package top.zenyoung.segment.distributor;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.segment.exception.NotFoundMaxIdException;
import top.zenyoung.segment.exception.SegmentNameMissingException;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Jdbc分布式分段ID实现
 *
 * @author young
 */
@Slf4j
public class JdbcIdSegmentDistributor implements IdSegmentDistributor {
    public static final Integer MAX_STEP = 5000;
    public static final String INCREMENT_MAX_ID_SQL = "update tbl_segment_id set max_id=(max_id + ?), step = ?, version=unix_timestamp() where biz_type = ?";
    public static final String FETCH_MAX_ID_SQL = "select max_id from tbl_segment_id where biz_type = ?";

    private final String namespace;
    private final long step;
    private final DataSource dataSource;
    private final String incrementMaxIdSql;
    private final String fetchMaxIdSql;

    public JdbcIdSegmentDistributor(@Nonnull final String namespace, final long step, @Nonnull final DataSource dataSource) {
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
        IdSegmentDistributor.ensureStep(sep);
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (final PreparedStatement statement = connection.prepareStatement(incrementMaxIdSql)) {
                statement.setLong(1, step);
                statement.setLong(2, step);
                statement.setString(3, getNamespace());
                final int affected = statement.executeUpdate();
                if (affected == 0) {
                    throw new SegmentNameMissingException(getNamespace());
                }
            }
            long nextMaxId;
            try (final PreparedStatement statement = connection.prepareStatement(fetchMaxIdSql)) {
                statement.setString(1, getNamespace());
                try (final ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new NotFoundMaxIdException(getNamespace());
                    }
                    nextMaxId = resultSet.getLong(1);
                }
            }
            connection.commit();
            return nextMaxId;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
