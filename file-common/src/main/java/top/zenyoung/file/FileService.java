package top.zenyoung.file;

import org.apache.commons.io.FilenameUtils;
import top.zenyoung.file.dto.DirectDTO;
import top.zenyoung.file.vo.DirectVO;
import top.zenyoung.file.vo.FileVO;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Objects;

/**
 * 文件-服务接口
 *
 * @author young
 */
public interface FileService {
    /**
     * 检查文件是否存在
     *
     * @param bucket 文件桶
     * @param key    文件键
     * @return 是否存在
     */
    boolean checkFileExists(@Nonnull final String bucket, @Nonnull final String key);

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
     * @param bucket 上传桶
     * @param key    上传键
     * @param file   上传文件
     * @return 上传结果
     * @throws IOException 上传异常
     */
    default FileVO upload(@Nonnull final String bucket, @Nonnull final String key, @Nonnull final File file) throws IOException {
        try (final FileInputStream input = new FileInputStream(file)) {
            final String fileName = FilenameUtils.getName(file.getName());
            return upload(bucket, key, fileName, input);
        }
    }

    /**
     * 上传文件
     *
     * @param bucket   上传桶
     * @param key      上传键
     * @param fileName 文件名
     * @param input    文件流
     * @return 上传结果
     */
    FileVO upload(@Nonnull final String bucket, @Nonnull final String key, @Nonnull final String fileName, @Nonnull final InputStream input);

    /**
     * 获取文件流
     *
     * @param bucket 文件桶
     * @param key    文件键
     * @return 文件流
     */
    InputStream getFileStream(@Nonnull final String bucket, @Nonnull final String key);

    /**
     * 获取文件字节数据
     *
     * @param bucket 文件桶
     * @param key    文件键
     * @return 文件字节数据
     */
    default byte[] getFileBytes(@Nonnull final String bucket, @Nonnull final String key) throws IOException {
        final InputStream input = getFileStream(bucket, key);
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
        return new byte[0];
    }

    /**
     * 复制文件
     *
     * @param sourceBucket 源Bucket
     * @param sourceKey    源文件键
     * @param targetBucket 目标Bucket
     * @param targetKey    目标文件键
     */
    void copyFile(@Nonnull final String sourceBucket, @Nonnull final String sourceKey, @Nonnull final String targetBucket, @Nonnull final String targetKey);

    /**
     * 删除文件
     *
     * @param bucket 文件桶
     * @param keys   文件键集合
     */
    void deleteFiles(@Nonnull final String bucket, @Nonnull final String... keys);
}
