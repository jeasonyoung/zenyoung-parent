package top.zenyoung.generator.service.impl;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.common.util.MapUtils;
import top.zenyoung.generator.domain.Column;
import top.zenyoung.generator.domain.Table;
import top.zenyoung.generator.model.DatabaseConnect;
import top.zenyoung.generator.service.DatabaseConnectService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库连接-服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
public class DatabaseConnectServiceImpl implements DatabaseConnectService {
    private static final Cache<DatabaseConnect, JdbcTemplate> DATA_SOURCE_CACHE = CacheUtils.createCache(50, Duration.ofMinutes(30));
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Map<String, Class<?>> DATA_TYPE_MAPS = new LinkedHashMap<>() {
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
    private static final String SQL_TABLE = "select table_name,table_comment,create_time,update_time from information_schema.tables where table_schema=(select database())";

    @Override
    public void testDatabaseConnect(@Nonnull final DatabaseConnect connect) {
        try {
            final JdbcTemplate template = buildJdbcTemplate(connect);
            if (template == null) {
                throw new RuntimeException("连接数据库失败!");
            }
            //连接查询测试
            final Integer val = template.queryForObject("select 1", Integer.class);
            log.info("testDatabaseConnect=> {}", val);
        } catch (Throwable ex) {
            //移除缓存
            DATA_SOURCE_CACHE.invalidate(connect);
            throw ex;
        }
    }

    @Override
    public Table getTable(@Nonnull final DatabaseConnect connect, @Nonnull final String tableName) {
        if (log.isDebugEnabled()) {
            log.debug("getTable(connect: {},tableName: {})...", connect, tableName);
        }
        Assert.hasText(tableName, "'tableName'不能为空!");
        final String sql = SQL_TABLE + " and table_name='" + tableName + "';";
        final List<Map<String, Object>> items = queryHandler(connect, sql);
        if (!CollectionUtils.isEmpty(items)) {
            return convertTable(items.get(0));
        }
        return null;
    }

    @Override
    public List<Table> queryTables(@Nonnull final DatabaseConnect connect, @Nullable final String queryTableName) {
        if (log.isDebugEnabled()) {
            log.debug("queryTables(connect: {},queryTableName: {})...", connect, queryTableName);
        }
        String query = SQL_TABLE;
        if (!Strings.isNullOrEmpty(queryTableName)) {
            query += " and (table_name like concat('%','" + queryTableName + "','%') or table_comment like concat('%','" + queryTableName + "','%'))";
        }
        final List<Map<String, Object>> items = queryHandler(connect, query);
        if (!CollectionUtils.isEmpty(items)) {
            return items.stream()
                    .map(this::convertTable)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private Table convertTable(@Nullable final Map<String, Object> map) {
        if (!CollectionUtils.isEmpty(map)) {
            final Table t = new Table();
            //表名
            t.setTableName(MapUtils.getVal(map, "table_name", String.class));
            //表描述
            t.setTableComment(MapUtils.getVal(map, "table_comment", String.class));
            //创建时间
            t.setCreateTime(MapUtils.getVal(map, "create_time", val -> {
                if (val instanceof Date) {
                    return (Date) val;
                }
                return null;
            }));
            //更新时间
            t.setUpdateTime(MapUtils.getVal(map, "update_time", val -> {
                if (val instanceof Date) {
                    return (Date) val;
                }
                return null;
            }));
            return t;
        }
        return null;
    }

    @Override
    public List<Column> getColumns(@Nonnull final DatabaseConnect connect, @Nonnull final String tableName) {
        if (log.isDebugEnabled()) {
            log.debug("getColumns(connect: {},tableName: {})...", connect, tableName);
        }
        if (!Strings.isNullOrEmpty(tableName)) {
            final String query = "select column_name,column_comment,column_type,"
                    + " if(is_nullable = 'no' and column_key != 'PRI', 1, 0) as is_required,"
                    + " if(column_key = 'PRI', '1', '0') as is_pk,"
                    + " ordinal_position as sort,"
                    + " if(extra = 'auto_increment', '1', '0') as is_increment "
                    + " from information_schema.columns"
                    + " where table_schema = (select database()) and table_name = '" + tableName + "'"
                    + " order by ordinal_position";
            final List<Map<String, Object>> items = queryHandler(connect, query);
            if (!CollectionUtils.isEmpty(items)) {
                return items.parallelStream().filter(Objects::nonNull)
                        .map(map -> {
                            final Column col = new Column();
                            //排序
                            col.setCode(MapUtils.getVal(map, "sort", val -> {
                                try {
                                    return Integer.parseInt(val.toString());
                                } catch (Throwable ex) {
                                    log.warn("getColumns(sort: {})-exp: {}", val, ex.getMessage());
                                }
                                return null;
                            }));
                            //列名
                            col.setColumnName(MapUtils.getVal(map, "column_name", val -> val == null ? "" : val.toString()));
                            //列描述
                            col.setColumnComment(MapUtils.getVal(map, "column_comment", val -> val == null ? "" : val.toString()));
                            //列类型
                            col.setColumnType(MapUtils.getVal(map, "column_type", val -> {
                                final String type = val.toString();
                                if (!Strings.isNullOrEmpty(type)) {
                                    final String typeVal = type.toLowerCase();
                                    for (Map.Entry<String, Class<?>> entry : DATA_TYPE_MAPS.entrySet()) {
                                        if (typeVal.startsWith(entry.getKey())) {
                                            //是否为时间戳
                                            if(!col.isTimestamp()) {
                                                col.setTimestamp("timestamp".equalsIgnoreCase(type));
                                            }
                                            return entry.getValue();
                                        }
                                    }
                                }
                                return String.class;
                            }));
                            //是否必须
                            col.setRequired(Boolean.TRUE.equals(MapUtils.getVal(map, "is_required", val -> {
                                try {
                                    final String s = val.toString();
                                    if (!Strings.isNullOrEmpty(s)) {
                                        return Integer.parseInt(s) > 0;
                                    }
                                } catch (Throwable ex) {
                                    log.warn("is_required({})-exp: {}", val, ex.getMessage());
                                }
                                return false;
                            })));
                            //是否为主键
                            col.setPrimaryKey(Boolean.TRUE.equals(MapUtils.getVal(map, "is_pk", val -> {
                                try {
                                    final String s = val.toString();
                                    if (!Strings.isNullOrEmpty(s)) {
                                        return Integer.parseInt(s) > 0;
                                    }
                                } catch (Throwable ex) {
                                    log.warn("is_pk({})-exp: {}", val, ex.getMessage());
                                }
                                return false;
                            })));
                            //是否为自增列
                            col.setIncrement(Boolean.TRUE.equals(MapUtils.getVal(map, "is_increment", val -> {
                                try {
                                    final String s = val.toString();
                                    if (!Strings.isNullOrEmpty(s)) {
                                        return Integer.parseInt(s) > 0;
                                    }
                                } catch (Throwable ex) {
                                    log.warn("is_increment({})-exp: {}", val, ex.getMessage());
                                }
                                return false;
                            })));
                            return col;
                        })
                        .sorted(Comparator.comparingInt(Column::getCode))
                        .collect(Collectors.toList());
            }
        }
        return null;
    }

    private List<Map<String, Object>> queryHandler(@Nonnull final DatabaseConnect connect, @Nonnull final String sql) {
        if (!Strings.isNullOrEmpty(sql)) {
            final String key = DigestUtils.md5Hex(connect + sql);
            synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
                try {
                    final JdbcTemplate jdbcTemplate = buildJdbcTemplate(connect);
                    return jdbcTemplate.queryForList(sql);
                } finally {
                    LOCKS.remove(key);
                }
            }
        }
        return null;
    }

    private static JdbcTemplate buildJdbcTemplate(@Nonnull final DatabaseConnect connect) {
        return CacheUtils.getCacheValue(DATA_SOURCE_CACHE, connect, () -> {
            final HikariDataSource dataSource = DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url(connect.getConnectString())
                    .username(connect.getAccount())
                    .password(connect.getPasswd())
                    .build();
            if (dataSource != null) {
                final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
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
            return null;
        });
    }
}
