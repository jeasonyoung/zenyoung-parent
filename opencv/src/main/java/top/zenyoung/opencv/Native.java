package top.zenyoung.opencv;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

/**
 * 本地链接库工具
 *
 * @author young
 */
@Slf4j
public class Native {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();

    static {
        loadNativeLibrary();
    }

    private static void loadNativeLibrary() {
        final String libName = System.mapLibraryName("opencv_java480");
        try {
            //加载文件地址
            final File libPath = copyResourceToTemp("native/" + libName);
            //本地库文件路径
            final String path = libPath.getAbsolutePath();
            //加载动态链接库
            System.load(path);
            //
            log.info("OpenCV native库 加载完成: {}", path);
        } catch (IOException e) {
            log.error("openCV native库 加载失败: {} => {}", libName, e.getMessage());
        }
    }

    public static File copyResourceToTemp(@Nonnull final String path) throws IOException {
        final String fileName = FilenameUtils.getName(path);
        final File tempFile = new File(FileUtils.getTempDirectory(), fileName);
        synchronized (LOCKS.computeIfAbsent(fileName, k -> new Object())) {
            try {
                //检查文件是否存在
                if (!tempFile.isFile()) {
                    //不为文件且存在及删除
                    if (tempFile.exists()) {
                        log.info("删除路径: {}", tempFile.getAbsolutePath());
                        Files.delete(tempFile.toPath());
                    }
                    final Class<?> cls = Native.class;
                    final ClassLoader cl = cls.getClassLoader();
                    final URL url = cl.getResource(path);
                    if (Objects.isNull(url)) {
                        final String err = "class path resource [" + path + "] cannot be resolved to URL because it does not exist";
                        throw new FileNotFoundException(err);
                    }
                    log.info("资源文件 url: {}", url);
                    //复制文件
                    FileUtils.copyToFile(url.openStream(), tempFile);
                    log.info("复制文件完成: {}=> {}", url, tempFile.getAbsolutePath());
                }
            } catch (IOException e) {
                log.error("copyResourceToTemp-复制文件失败[{}]-exp: {}", fileName, e.getMessage());
                throw e;
            } finally {
                LOCKS.remove(fileName);
            }
        }
        return tempFile;
    }
}
