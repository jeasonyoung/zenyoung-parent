package top.zenyoung.framework.generator.db;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.SpiUtils;
import top.zenyoung.framework.generator.type.ColumnType;
import top.zenyoung.framework.generator.type.DbTypeConvert;
import top.zenyoung.framework.generator.util.NameUtils;
import top.zenyoung.framework.generator.vo.TableVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据查询
 *
 * @author young
 */
@Slf4j
public class DbQuery {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private final JdbcTemplate jdbcTemplate;
    private final QueryInfo queryInfo;
    private final DbTypeConvert typeConvert;

    /**
     * 构造函数
     *
     * @param dataSource 数据源
     */
    public DbQuery(@Nonnull final DataSource dataSource) {
        this.queryInfo = SpiUtils.load(QueryInfo.class);
        this.typeConvert = SpiUtils.load(DbTypeConvert.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private List<Map<String, Object>> queryHandler(@Nonnull final String sql, @Nullable final Object... args) {
        if (!Strings.isNullOrEmpty(sql)) {
            final long start = System.currentTimeMillis();
            synchronized (LOCKS.computeIfAbsent(sql, k -> new Object())) {
                try {
                    if (Objects.nonNull(args) && args.length > 0) {
                        return dataFieldFormat(jdbcTemplate.queryForList(sql, args));
                    }
                    return dataFieldFormat(jdbcTemplate.queryForList(sql));
                } finally {
                    LOCKS.remove(sql);
                    log.info("queryHandler[执行时间: {}ms]-sql: {}, args: {}", sql, args, (System.currentTimeMillis() - start));
                }
            }
        }
        return Lists.newArrayList();
    }

    private List<Map<String, Object>> dataFieldFormat(final List<Map<String, Object>> items) {
        if (!CollectionUtils.isEmpty(items)) {
            return items.parallelStream()
                    .map(map -> {
                        final Map<String, Object> out = Maps.newHashMap();
                        map.forEach((k, v) -> {
                            if (!Strings.isNullOrEmpty(k) && Objects.nonNull(v)) {
                                out.put(k.toUpperCase(), v);
                            }
                        });
                        return out;
                    })
                    .collect(Collectors.toList());
        }
        return items;
    }

    private void switchDatabase(@Nullable final String dbName) {
        if (!Strings.isNullOrEmpty(dbName)) {
            final long start = System.currentTimeMillis();
            final String sql = String.format("use `%s`;", dbName);
            synchronized (LOCKS.computeIfAbsent(sql, k -> new Object())) {
                try {
                    jdbcTemplate.execute(sql);
                } finally {
                    LOCKS.remove(sql);
                    log.info("switchDatabase[执行时间: {}ms]-切换数据库: {}", (System.currentTimeMillis() - start), dbName);
                }
            }
        }
    }

    /**
     * 获取数据库名集合
     *
     * @return 数据库名集合
     */
    public List<String> getDatabases() {
        return queryHandler(queryInfo.databasesSql()).stream()
                .map(m -> {
                    final List<Object> vals = Lists.newArrayList(m.values());
                    if (!CollectionUtils.isEmpty(vals)) {
                        final Object val = vals.get(0);
                        if (Objects.nonNull(val)) {
                            if (val instanceof String) {
                                return (String) val;
                            }
                            return val.toString();
                        }
                    }
                    return null;
                })
                .filter(val -> !Strings.isNullOrEmpty(val))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取数据库中的表集合
     *
     * @param dbName 数据库名
     * @return 表集合
     */
    public List<TableVO> getAllTables(@Nullable final String dbName) {
        //切换数据库
        switchDatabase(dbName);
        //查询表集合
        return queryHandler(queryInfo.tablesSql()).stream()
                .map(tb -> {
                    final ResultWrapper wrapper = ResultWrapper.of(tb);
                    return TableVO.builder()
                            //表名
                            .name(wrapper.getFieldValue(queryInfo.tableName()))
                            //表注解
                            .comment(wrapper.getComment(queryInfo.tableComment()))
                            .build();
                })
                .sorted(Comparator.comparing(TableVO::getName))
                .collect(Collectors.toList());
    }

    /**
     * 获取表数据
     *
     * @param dbName      数据库名
     * @param matchTables 匹配表集合
     * @return 表数据集合
     */
    public List<Table> getTables(@Nullable final String dbName, @Nullable final List<String> matchTables) {
        //切换数据库
        switchDatabase(dbName);
        //查询数据表集合
        return queryHandler(queryInfo.tablesSql()).stream()
                .map(tb -> {
                    final ResultWrapper wrapper = ResultWrapper.of(tb);
                    final String tableName = wrapper.getFieldValue(queryInfo.tableName());
                    if (!Strings.isNullOrEmpty(tableName) && matchTable(tableName, matchTables)) {
                        final Table table = new Table();
                        //表名
                        table.setName(tableName);
                        //表注释
                        table.setComment(wrapper.getComment(queryInfo.tableComment()));
                        //填充表字段集合
                        buildTableFields(table);
                        //初始化导包处理
                        table.init();
                        //
                        return table;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Table::getName))
                .collect(Collectors.toList());
    }

    private void buildTableFields(@Nonnull final Table table) {
        table.setFields(
                queryHandler(queryInfo.tableFieldsSql(table.getName())).stream()
                        .map(fields -> {
                            //转换包装器
                            final ResultWrapper wrapper = ResultWrapper.of(fields);
                            //字段名
                            final String name = wrapper.getFieldValue(queryInfo.fieldName());
                            //字段数据类型
                            final String type = wrapper.getFieldValue(queryInfo.fieldType());
                            //字段Java数据类型
                            final ColumnType columnType = typeConvert.processTypeConvert(type);
                            //
                            return TableField.builder()
                                    //字段名
                                    .name(name)
                                    //字段描述
                                    .comment(wrapper.getComment(queryInfo.fieldComment()))
                                    //是否主键
                                    .isPrimaryKey(wrapper.isPrimaryKey(queryInfo.fieldKey()))
                                    //字段数据类型
                                    .type(type)
                                    //字段Java数据类型
                                    .columnType(columnType)
                                    //字段Java数据类型
                                    .propertyType(columnType.getType())
                                    //驼峰式字段名
                                    .propertyName(NameUtils.underlineToCamel(name))
                                    .build();
                        })
                        .collect(Collectors.toList())
        );
    }


    /**
     * 表名匹配
     *
     * @param tableName   表名
     * @param matchTables 匹配集合
     * @return 是否匹配
     */
    private boolean matchTable(@Nonnull final String tableName, @Nullable final List<String> matchTables) {
        if (!CollectionUtils.isEmpty(matchTables)) {
            return matchTables.stream().anyMatch(tableName::equalsIgnoreCase);
        }
        return true;
    }
}
