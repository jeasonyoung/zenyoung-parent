package top.zenyoung.file.service;

import org.apache.commons.io.FilenameUtils;
import top.zenyoung.file.dto.DirectDTO;
import top.zenyoung.file.vo.DirectVO;
import top.zenyoung.file.vo.FileVO;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * 配置文件桶-文件服务接口
 *
 * @author young
 */
public interface BucketFileService {
    /**
     * 检查文件是否存在
     *
     * @param key 文件键
     * @return 是否存在
     */
    boolean checkFileExists(@Nonnull final String key);

    /**
     * 直传文件签名处理(客户端直传,服务端签名)
     *
     * @param dto 直传DTO
     * @return 直传签名
     */
    DirectVO directUploadSignature(@Nonnull final DirectDTO dto);

    /**
     * 上传文件
     *
     * @param key  上传键
     * @param file 上传文件
     * @return 上传结果
     * @throws Exception 上传异常
     */
    default FileVO upload(@Nonnull final String key, @Nonnull final File file) throws Exception {
        try (final FileInputStream input = new FileInputStream(file)) {
            final String fileName = FilenameUtils.getName(file.getName());
            return upload(key, fileName, input);
        }
    }

    /**
     * 上传文件
     *
     * @param key      上传键
     * @param fileName 文件名
     * @param input    文件流
     * @return 上传结果
     */
    FileVO upload(@Nonnull final String key, @Nonnull final String fileName, @Nonnull final InputStream input);

    /**
     * 获取文件流
     *
     * @param key 文件键
     * @return 文件流
     */
    InputStream getFileStream(@Nonnull final String key);

    /**
     * 获取文件字节数据
     *
     * @param key 文件键
     * @return 文件字节数据
     */
    default byte[] getFileBytes(@Nonnull final String key) throws Exception {
        final InputStream input = getFileStream(key);
        if (Objects.nonNull(input)) {
            try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                final byte[] buf = new byte[1024];
                int count;
                while ((count = input.read(buf, 0, buf.length)) != -1) {
                    output.write(buf, 0, count);
                }
                return output.toByteArray();
            }
        }
        return null;
    }

    /**
     * 复制文件
     *
     * @param sourceKey 源文件键
     * @param targetKey 目标文件键
     */
    void copyFile(@Nonnull final String sourceKey, @Nonnull final String targetKey);

    /**
     * 删除文件
     *
     * @param keys 文件键集合
     */
    void deleteFiles(@Nonnull final String... keys);
}