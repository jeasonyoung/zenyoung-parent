package top.zenyoung.generator.service.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.generator.config.GenConfig;
import top.zenyoung.generator.domain.Column;
import top.zenyoung.generator.domain.Table;
import top.zenyoung.generator.service.GeneratorCodeService;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 代码生成-服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratorCodeServiceImpl implements GeneratorCodeService {
    private static final Cache<String, String> TABLE_REMOVE_PREFIX_CACHE = CacheUtils.createCache(10, Duration.ofMinutes(5));
    private static final String SEP = ",", DB_SEP = "_", LOGIC_DEL = "status";

    private static final List<Pair<String, String>> CODE_TEMPLATES = new LinkedList<>() {
        {
            add(Pair.of("java/entity.java.ftl", ".dao.entity"));
            add(Pair.of("java/jpa.java.ftl", ".dao.jpa"));
            add(Pair.of("java/dto.java.ftl", ".dao.dto"));
            add(Pair.of("java/repository.java.ftl", ".dao.repository"));
            add(Pair.of("java/repositoryImpl.java.ftl", ".dao.repository.impl"));
        }
    };

    private final Configuration configuration;
    private final GenConfig config;

    @SneakyThrows
    @Override
    public Map<String, String> generatorCodes(@Nonnull final Table table, @Nonnull final List<Column> columns) {
        final GenTable genTable = GenTable.of().init(table, columns, config);

        final Template template = configuration.getTemplate("");

        return null;
    }

    private static String removeTablePrefix(@Nonnull final Table table, @Nonnull final GenConfig config) {
        final String tableName = table.getTableName();
        if (!Strings.isNullOrEmpty(tableName)) {
            return CacheUtils.getCacheValue(TABLE_REMOVE_PREFIX_CACHE, tableName, () -> {
                final boolean autoRemovePrefix = config.getAutoRemovePrefix();
                final String tablePrefix = config.getTablePrefix();
                if (autoRemovePrefix && !Strings.isNullOrEmpty(tablePrefix)) {
                    final List<String> prefixs = Splitter.on(SEP).omitEmptyStrings().trimResults().splitToList(tableName);
                    if (!CollectionUtils.isEmpty(prefixs)) {
                        String copyTableName = tableName;
                        for (String prefix : prefixs) {
                            if (!Strings.isNullOrEmpty(prefix) && copyTableName.startsWith(prefix)) {
                                copyTableName = copyTableName.replaceFirst(prefix, "");
                            }
                        }
                        return copyTableName;
                    }
                }
                return tableName;
            });
        }
        return tableName;
    }

    private static String convertClassName(@Nonnull final Table table, @Nonnull final GenConfig config) {
        return toCamelCase(removeTablePrefix(table, config));
    }

    private static String toCamelCase(@Nonnull final String name) {
        if (!Strings.isNullOrEmpty(name)) {
            //如果不含下划线，仅将首字母大写
            if (!name.contains(DB_SEP)) {
                return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            }
            final StringBuilder builder = new StringBuilder();
            final List<String> words = Splitter.on(DB_SEP).omitEmptyStrings().trimResults().splitToList(name);
            if (!CollectionUtils.isEmpty(words)) {
                words.forEach(word -> {
                    if (!Strings.isNullOrEmpty(word)) {
                        builder.append(toCamelCase(word));
                    }
                });
            }
            return builder.toString();
        }
        return name;
    }

    private static String convertModuleName(@Nonnull final String packageName) {
        if (!Strings.isNullOrEmpty(packageName)) {
            final int idx = packageName.lastIndexOf(".");
            if (idx > -1) {
                return packageName.substring(idx + 1);
            }
        }
        return null;
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    private static class GenTable implements Serializable {
        /**
         * 包名
         */
        private String packageName;
        /**
         * 表名
         */
        private String tableName;
        /**
         * 类名
         */
        private String className;
        /**
         * 小写类名
         */
        private String lowerClassName;
        /**
         * 模块名
         */
        private String moduleName;
        /**
         * 描述
         */
        private String comment;
        /**
         * 作者
         */
        private String author;
        /**
         * java导入集合
         */
        private List<String> javaImports;
        /**
         * 字段集合
         */
        private List<GenTableColumn> columns;

        /**
         * 是否有逻辑删除
         */
        private boolean logicDelete;

        public GenTable init(@Nonnull final Table table, @Nonnull final List<Column> columns, @Nonnull final GenConfig config) {
            this.packageName = config.getPackageName();
            this.tableName = table.getTableName();
            this.className = convertClassName(table, config);
            final String clsName;
            if (!Strings.isNullOrEmpty(clsName = this.className)) {
                this.lowerClassName = clsName.substring(0, 1).toLowerCase() + (clsName.length() > 1 ? clsName.substring(1) : "");
            }
            this.moduleName = convertModuleName(config.getPackageName());
            this.comment = table.getTableComment();
            this.author = config.getAuthor();
            this.javaImports = Lists.newLinkedList();
            this.columns = columns.stream()
                    .filter(Objects::nonNull)
                    .map(col -> GenTableColumn.of().init(this, col))
                    .sorted(Comparator.comparingInt(GenTableColumn::getOrder))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(this.javaImports)) {
                this.javaImports.sort(Comparator.comparing(String::intern));
            }
            return this;
        }
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    private static class GenTableColumn implements Serializable {
        /**
         * 表字段名
         */
        private String columnName;
        /**
         * Java字段名
         */
        private String javaField;
        /**
         * Java字段类型名称
         */
        private String javaType;
        /**
         * 描述
         */
        private String comment;
        /**
         * 是否必须
         */
        private boolean required;
        /**
         * 是否为主键
         */
        private boolean primaryKey;
        /**
         * 是否为自增列
         */
        private boolean increment;
        /**
         * 是否为时间戳类型
         */
        private boolean timestamp;
        /**
         * 排序
         */
        private int order;

        public GenTableColumn init(@Nonnull final GenTable genTable, @Nonnull final Column column) {
            this.columnName = column.getColumnName();
            //是否支持逻辑删除
            genTable.logicDelete = LOGIC_DEL.equalsIgnoreCase(this.columnName);
            this.javaField = toCamelCase(column.getColumnName());
            final String name;
            if (!Strings.isNullOrEmpty(name = this.javaField)) {
                this.javaField = name.substring(0, 1).toLowerCase() + (name.length() > 1 ? name.substring(1) : "");
            }
            final Class<?> cls = column.getColumnType();
            if (cls != null) {
                this.javaType = cls.getSimpleName();
                final Package p = cls.getPackage();
                if (p != null) {
                    final String importName = p.getName();
                    if (!Strings.isNullOrEmpty(importName) && !genTable.javaImports.contains(importName)) {
                        genTable.javaImports.add(importName);
                    }
                }
            }
            this.comment = column.getColumnComment();
            this.required = column.isRequired();
            this.primaryKey = column.isPrimaryKey();
            this.increment = column.isIncrement();
            this.timestamp = column.isTimestamp();
            this.order = column.getCode();
            return this;
        }
    }
}
