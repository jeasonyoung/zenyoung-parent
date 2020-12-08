package top.zenyoung.common.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zip压缩工具类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/11 10:55 上午
 **/
@Slf4j
public class ZipUtils {

    /**
     * 将目录压缩为ZIP文件
     *
     * @param sourceDirs       压缩源目录
     * @param outputStream     ZIP文件输出流
     * @param keepDirStructure 是否保持文件目录结构
     */
    public static void toZip(@Nonnull final File sourceDirs, @Nonnull final OutputStream outputStream, @Nonnull final Boolean keepDirStructure) {
        log.debug("toZip(sourceDirs: {},keepDirStructure: {})...", sourceDirs, keepDirStructure);
        toZipHandler(outputStream, zipOutputStream -> compress(sourceDirs, sourceDirs.getName(), zipOutputStream, keepDirStructure));
    }

    /**
     * 将文件集合压缩为ZIP文件
     *
     * @param sourceFiles  压缩源文件集合
     * @param outputStream ZIP文件输出流
     */
    public static void toZip(@Nonnull final List<File> sourceFiles, @Nonnull final OutputStream outputStream) {
        log.debug("toZip(sourceFiles-count: {})...", sourceFiles.size());
        Assert.notEmpty(sourceFiles, "'sourceFiles'不能为空!");
        toZipHandler(outputStream, zipOutputStream -> {
            for (File file : sourceFiles) {
                if (file == null) {
                    continue;
                }
                //压缩处理
                compress(file, file.getName(), zipOutputStream, false);
            }
        });
    }

    private static void toZipHandler(@Nonnull final OutputStream outputStream, @Nonnull final Consumer<ZipOutputStream> outputStreamConsumer) {
        final long start = System.currentTimeMillis();
        try (final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            //业务处理
            outputStreamConsumer.accept(zipOutputStream);
            //耗时打印
            log.info("toZipHandler(outputStreamConsumer: {})-压缩耗时: {}", outputStreamConsumer, (System.currentTimeMillis() - start));
        } catch (IOException ex) {
            log.error("toZipHandler(outputStreamConsumer: {})-exp: {}", outputStreamConsumer, ex.getMessage());
            throw new RuntimeException(ex);
        }

    }

    @SneakyThrows({IOException.class})
    private static void compress(@Nonnull final File sourceFile, @Nonnull final String name, @Nonnull final ZipOutputStream zipOutputStream, @Nonnull final Boolean keepDirStructure) {
        //文件处理
        if (sourceFile.isFile()) {
            //向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zipOutputStream.putNextEntry(new ZipEntry(name));
            //复制文件到Zip输出流中
            FileUtils.copyFile(sourceFile, zipOutputStream);
            //关闭Zip实体
            zipOutputStream.closeEntry();
            return;
        }
        //目录处理
        if (sourceFile.isDirectory()) {
            final File[] childs = sourceFile.listFiles();
            //空文件夹处理
            if (childs == null || childs.length == 0) {
                //是否需要保留文件目录结构
                if (keepDirStructure) {
                    //空文件夹处理
                    zipOutputStream.putNextEntry(new ZipEntry(name + "/"));
                    //没有文件不需要复制文件内容
                    zipOutputStream.closeEntry();
                }
                return;
            }
            //文件集合处理
            for (File child : childs) {
                if (child == null) {
                    continue;
                }
                final String childName = child.getName();
                compress(child, keepDirStructure ? name + "/" + childName : childName, zipOutputStream, keepDirStructure);
            }
        }
    }
}
