package top.zenyoung.generator.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.generator.config.GenConfig;
import top.zenyoung.generator.domain.Column;
import top.zenyoung.generator.domain.Table;
import top.zenyoung.generator.service.GeneratorCodeService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成-服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratorCodeServiceImpl implements GeneratorCodeService {
    private static final String LOGIC_DEL = "status";
    private static final String DB_SEP = "_";
    private static final String TEMPLATE_SUFFIX = ".ftl";

    private static final List<Pair<String, String>> CODE_TEMPLATES = new LinkedList<Pair<String, String>>() {
        {
            add(Pair.of("java/entity.java" + TEMPLATE_SUFFIX, ".dao.entity"));
            add(Pair.of("java/jpa.java" + TEMPLATE_SUFFIX, ".dao.jpa"));
            add(Pair.of("java/dto.java" + TEMPLATE_SUFFIX, ".dao.dto"));
            add(Pair.of("java/repository.java" + TEMPLATE_SUFFIX, ".dao.repository"));
            add(Pair.of("java/repositoryImpl.java" + TEMPLATE_SUFFIX, ".dao.repository.impl"));
            add(Pair.of("java/req.java" + TEMPLATE_SUFFIX, ".vo"));
            add(Pair.of("java/res.java" + TEMPLATE_SUFFIX, ".vo"));
            add(Pair.of("java/controller.java" + TEMPLATE_SUFFIX, ".controller"));
        }
    };

    private final Configuration configuration;
    private final GenConfig config;

    private final ObjectMapper mapper;

    @Override
    public Map<String, String> generatorCodes(@Nonnull final Table table, @Nonnull final List<Column> columns) {
        final GenTable genTable = GenTable.of().init(table, columns, config);
        if (!CollectionUtils.isEmpty(CODE_TEMPLATES)) {
            final Map<String, Object> data = genTable.toMap(mapper);
            if (!CollectionUtils.isEmpty(data)) {
                return CODE_TEMPLATES.stream().filter(Objects::nonNull)
                        .map(p -> {
                            final String fileName;
                            if (!Strings.isNullOrEmpty(fileName = p.getFirst()) && !Strings.isNullOrEmpty(p.getSecond())) {
                                try (final StringWriter sw = new StringWriter()) {
                                    final Template template = configuration.getTemplate(p.getFirst(), "UTF-8");
                                    if (template != null) {
                                        //渲染模板
                                        template.process(data, sw);
                                        final int idx = fileName.indexOf("/");
                                        final String endClassName = (idx > 0 ? fileName.substring(idx + 1) : fileName).replaceFirst(TEMPLATE_SUFFIX, "").trim();
                                        final String className = genTable.getClassName() + endClassName.substring(0, 1).toUpperCase() + endClassName.substring(1);
                                        return Pair.of(genTable.getPackageName() + p.getSecond() + "." + className, sw.toString());
                                    }
                                } catch (Throwable ex) {
                                    log.error("generatorCodes(ftl: {})-exp: {}", p.getFirst(), ex.getMessage());
                                }
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (n, o) -> n));
            }
        }
        return null;
    }

    private static String removeTablePrefix(@Nonnull final Table table, @Nonnull final GenConfig config) {
        final String tableName = table.getTableName();
        if (!Strings.isNullOrEmpty(tableName)) {
            final boolean autoRemovePrefix = config.getAutoRemovePrefix();
            final String tablePrefix = config.getTablePrefix();
            if (autoRemovePrefix && !Strings.isNullOrEmpty(tablePrefix) && tableName.startsWith(tablePrefix)) {
                return tableName.replaceFirst(tablePrefix, "");
            }
        }
        return tableName;
    }

    private static String convertClassName(@Nonnull final Table table, @Nonnull final GenConfig config) {
        return toCamelCase(removeTablePrefix(table, config));
    }

    private static String toCamelCase(@Nullable final String name) {
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

    @Override
    public void buildZipStream(@Nullable final Map<String, String> fileMaps, @Nonnull final OutputStream output) {
        if (!CollectionUtils.isEmpty(fileMaps)) {
            final Map<String, String> extProjects = new LinkedHashMap<String, String>() {
                {
                    put(".java", "src/main/java");
                }
            };
            final String cSep = ".", pSep = "/";
            try (final ZipOutputStream zip = new ZipOutputStream(output)) {
                fileMaps.forEach((key, value) -> {
                    final String ext = cSep + FilenameUtils.getExtension(key);
                    final String root = extProjects.getOrDefault(ext, "");
                    final int idx = key.replaceFirst(ext, "").lastIndexOf(cSep);
                    final String dirs = key.substring(0, idx).replace(cSep, pSep);
                    final String fileName = key.substring(idx + 1);
                    final String fullPathName = Joiner.on(pSep).skipNulls().join(root, dirs, fileName);
                    try {
                        zip.putNextEntry(new ZipEntry(fullPathName));
                        IOUtils.write(value, zip, StandardCharsets.UTF_8);
                        zip.closeEntry();
                    } catch (IOException e) {
                        log.error("buildZipStream(fullPathName: {})-exp: {}", fullPathName, e.getMessage());
                    }
                });
                zip.flush();
            } catch (Throwable ex) {
                log.error("buildZipStream(files: {})-exp: {}", fileMaps.keySet(), ex.getMessage());
            }
        }
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

        public Map<String, Object> toMap(@Nonnull final ObjectMapper mapper) {
            return JsonUtils.toMap(mapper, this, Object.class);
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
            if (!genTable.isLogicDelete()) {
                genTable.logicDelete = LOGIC_DEL.equalsIgnoreCase(this.columnName);
            }
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
