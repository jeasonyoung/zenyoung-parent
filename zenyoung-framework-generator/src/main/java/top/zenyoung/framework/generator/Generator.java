package top.zenyoung.framework.generator;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.CollectionUtils;
import top.zenyoung.framework.generator.db.DbQuery;
import top.zenyoung.framework.generator.db.Table;
import top.zenyoung.framework.generator.dto.GeneratorDTO;
import top.zenyoung.framework.generator.ftl.FtlFileInfo;
import top.zenyoung.framework.generator.util.FtlFileUtils;
import top.zenyoung.framework.generator.vo.FileVO;
import top.zenyoung.framework.generator.vo.TableVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器
 *
 * @author young
 */
@Slf4j
public class Generator {
    private final DbQuery query;

    /**
     * 构造函数
     *
     * @param dataSource 数据源
     */
    public Generator(@Nonnull final DataSource dataSource) {
        this.query = new DbQuery(dataSource);
    }

    /**
     * 获取数据库集合
     *
     * @return 数据库集合
     */
    public List<String> getDatabases() {
        return query.getDatabases();
    }

    /**
     * 获取数据库下表信息集合
     *
     * @param dbName 数据库名称
     * @return 表信息集合
     */
    public List<TableVO> getTables(@Nullable final String dbName) {
        return query.getAllTables(dbName);
    }

    /**
     * 生成代码
     *
     * @param dto 生成配置
     * @return 代码文件集合
     */
    public List<FileVO> build(@Nonnull final GeneratorDTO dto) {
        final List<FtlFileInfo> files = Lists.newArrayList();
        final List<FtlFileInfo> globals = FtlFileUtils.build(dto);
        if (!CollectionUtils.isEmpty(globals)) {
            files.addAll(globals);
        }
        //加载表数据
        final List<Table> tables = query.getTables(dto.getDbName(), dto.getIncludeTableNames());
        if (!CollectionUtils.isEmpty(tables)) {
            final List<FtlFileInfo> tbs = tables.stream()
                    .map(tb -> FtlFileUtils.build(dto, tb))
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(tbs)) {
                files.addAll(tbs);
            }
        }
        //生成处理
        return files.stream()
                .map(info -> {
                    final String content = info.buildFtlContent();
                    return FileVO.of(info.getFileDir(), info.getFileName(), content);
                })
                .sorted((o1, o2) -> {
                    final String dir1 = o1.getDir(), dir2 = o2.getDir();
                    int ret = dir1.compareToIgnoreCase(dir2);
                    if (ret == 0) {
                        final String v1 = dir1 + o1.getName(), v2 = dir2 + o2.getName();
                        ret = v1.compareToIgnoreCase(v2);
                    }
                    return ret;
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成代码文件压缩包
     *
     * @param dto    生成配置
     * @param output 输出流
     */
    public void buildZip(@Nonnull final GeneratorDTO dto, @Nonnull final OutputStream output) {
        final List<FileVO> files = build(dto);
        if (!CollectionUtils.isEmpty(files)) {
            final long start = System.currentTimeMillis();
            try (final ZipOutputStream zip = new ZipOutputStream(output)) {
                //构建压缩包
                files.forEach(fi -> {
                    final String dir = fi.getDir(), sep = File.separator;
                    final String fullPathName = dir + sep + fi.getName();
                    try {
                        final String entryName = fullPathName.startsWith(sep) ? fullPathName.substring(sep.length()) : fullPathName;
                        zip.putNextEntry(new ZipEntry(entryName));
                        IOUtils.write(fi.getContent(), zip, StandardCharsets.UTF_8);
                        zip.closeEntry();
                    } catch (IOException e) {
                        log.warn("构建压缩包文件[{}]失败: {}", fullPathName, e.getMessage());
                    }
                });
            } catch (Throwable ex) {
                log.error("生成压缩包异常:" + ex.getMessage());
            } finally {
                log.info("生成压缩包文件耗时: {}ms", (System.currentTimeMillis() - start));
            }
        }
    }

    /**
     * 生成代码文件
     *
     * @param dto 生成配置
     * @param dir 生成目录
     */
    public void buildFiles(@Nonnull final GeneratorDTO dto, @Nonnull final File dir) {
        final List<FileVO> files = build(dto);
        if (!CollectionUtils.isEmpty(files)) {
            final long start = System.currentTimeMillis();
            try {
                files.stream()
                        .filter(f -> Objects.nonNull(f) && !Strings.isNullOrEmpty(f.getName()) && !Strings.isNullOrEmpty(f.getContent()))
                        .forEach(f -> {
                            try {
                                final File fDir = Strings.isNullOrEmpty(f.getDir()) ? dir : new File(dir, f.getDir());
                                if (!fDir.exists()) {
                                    final boolean ret = fDir.mkdirs();
                                    log.info("创建目录[{}]: {}", ret, fDir.getAbsolutePath());
                                } else {
                                    //清空目录下文件
                                    FileUtils.cleanDirectory(fDir);
                                }
                                try (FileOutputStream outputStream = new FileOutputStream(new File(fDir, f.getName()))) {
                                    //内容写入文件
                                    IOUtils.write(f.getContent(), outputStream, StandardCharsets.UTF_8);
                                }
                            } catch (Throwable e) {
                                log.warn("生成代码文件({})-exp: {}", f.getName(), e.getMessage());
                            }
                        });
            } finally {
                log.info("生成代码文件耗时: {}ms", (System.currentTimeMillis() - start));
            }
        }
    }
}
