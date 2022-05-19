package top.zenyoung.framework.generator.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.framework.generator.domain.Column;
import top.zenyoung.framework.generator.domain.Table;
import top.zenyoung.framework.generator.service.DatabaseConnectService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 数据库连接-服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseConnectServiceImpl implements DatabaseConnectService {
    private static final String SQL_TABLE = "select table_name as tableName,table_comment as tableComment,create_time as createTime,update_time as updateTime " +
            " from information_schema.tables where table_schema=(select database())";
    private static final String SQL_COLUMN = "select column_name as columnName,column_comment as columnComment,column_type as columnType," +
            "if(is_nullable = 'no' and column_key != 'PRI', 1, 0) as required,if(column_key = 'PRI', 1, 0) as primaryKey," +
            "ordinal_position as code,if(extra = 'auto_increment', 1, 0) as increment " +
            " from information_schema.columns " +
            " where table_schema=(select database()) and table_name=? " +
            " order by ordinal_position";
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Map<String, Class<?>> DATA_TYPE_MAPS = new LinkedHashMap<String, Class<?>>() {
        {
            put("bigint", Long.class);
            put("date", Date.class);
            put("datetime", Date.class);
            put("timestamp", Date.class);
            put("int", Integer.class);
            put("smallint", Integer.class);
            put("tinyint", Integer.class);
        }
    };
    private final DataSource dataSource;

    @Override
    public void testDatabaseConnect() {
        try {
            final JdbcTemplate template = buildJdbcTemplate();
            //连接查询测试
            final Integer val = template.queryForObject("select 1", Integer.class);
            log.info("testDatabaseConnect=> {}", val);
        } catch (Throwable e) {
            throw new RuntimeException("连接数据库失败: " + e.getMessage());
        }
    }

    @Override
    public Table getTable(@Nonnull final String tableName) {
        Assert.hasText(tableName, "'tableName'不能为空!");
        final String sql = SQL_TABLE + " and table_name=?";
        final List<Map<String, Object>> items = queryHandler(sql, tableName);
        if (!CollectionUtils.isEmpty(items)) {
            return buildTable(items.get(0));
        }
        return null;
    }

    @Override
    public List<Table> queryTables(@Nullable final String queryTableName) {
        String query = SQL_TABLE;
        if (!Strings.isNullOrEmpty(queryTableName)) {
            query += " and (table_name like concat('%',?,'%') or table_comment like concat('%',?,'%'))";
        }
        final Object[] args = Strings.isNullOrEmpty(queryTableName) ? null : new Object[]{queryTableName, queryTableName};
        final List<Map<String, Object>> items = queryHandler(query, args);
        if (!CollectionUtils.isEmpty(items)) {
            return items.parallelStream()
                    .filter(Objects::nonNull)
                    .map(this::buildTable)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private Table buildTable(@Nonnull final Map<String, Object> map) {
        final Table t = new Table();
        final AtomicBoolean ref = new AtomicBoolean(false);
        ReflectionUtils.doWithFields(Table.class, field -> {
            final Object val = map.getOrDefault(field.getName(), null);
            if (Objects.nonNull(val)) {
                field.setAccessible(true);
                field.set(t, val);
                ref.set(true);
            }
        });
        return ref.get() ? t : null;
    }


    @Override
    public List<Column> getColumns(@Nonnull final String tableName) {
        final List<Map<String, Object>> items = queryHandler(SQL_COLUMN, tableName);
        if (!CollectionUtils.isEmpty(items)) {
            return items.parallelStream()
                    .filter(Objects::nonNull)
                    .map(this::buildColumn)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private Column buildColumn(@Nonnull final Map<String, Object> dataVals) {
        final List<String> excludes = Lists.newArrayList("columnType", "timestamp");
        final Column col = new Column();
        final AtomicBoolean ref = new AtomicBoolean(false);
        ReflectionUtils.doWithFields(Column.class, field -> {
            final Object val = dataVals.getOrDefault(field.getName(), null);
            if (Objects.nonNull(val)) {
                field.setAccessible(true);
                field.set(col, val);
                ref.set(true);
            }
        }, field -> !excludes.contains(field.getName()));
        //列类型
        final Object columnType = dataVals.getOrDefault("columnType", null);
        if (Objects.nonNull(columnType)) {
            final String type = columnType.toString().toLowerCase();
            final Class<?> typeClass = DATA_TYPE_MAPS.getOrDefault(type, null);
            if (Objects.nonNull(typeClass)) {
                col.setColumnType(typeClass);
                //是否为时间戳
                col.setTimestamp("timestamp".equalsIgnoreCase(type));
            }
        }
        return ref.get() ? col : null;
    }

    private List<Map<String, Object>> queryHandler(@Nonnull final String sql, @Nullable final Object... args) {
        if (!Strings.isNullOrEmpty(sql)) {
            log.info("queryHandler-sql: {}, args: {}", sql, args);
            synchronized (LOCKS.computeIfAbsent(sql, k -> new Object())) {
                try {
                    final JdbcTemplate jdbcTemplate = buildJdbcTemplate();
                    return (args == null || args.length == 0) ? jdbcTemplate.queryForList(sql) : jdbcTemplate.queryForList(sql, args);
                } finally {
                    LOCKS.remove(sql);
                }
            }
        }
        return null;
    }

    private JdbcTemplate buildJdbcTemplate() {
        Assert.notNull(this.dataSource, "未配置DataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
        final JdbcProperties properties = new JdbcProperties();
        final JdbcProperties.Template template = properties.getTemplate();
        jdbcTemplate.setFetchSize(template.getFetchSize());
        jdbcTemplate.setMaxRows(template.getMaxRows());
        final Duration duration;
        if ((duration = template.getQueryTimeout()) != null) {
            jdbcTemplate.setQueryTimeout((int) duration.getSeconds());
        }
        return jdbcTemplate;
    }
}
