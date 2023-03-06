package top.zenyoung.file.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import top.zenyoung.file.FileService;
import top.zenyoung.file.config.FileProperties;
import top.zenyoung.file.dto.DirectDTO;
import top.zenyoung.file.service.BucketFileService;
import top.zenyoung.file.vo.DirectVO;
import top.zenyoung.file.vo.FileVO;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.function.Function;

/**
 * 配置文件桶-文件服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BucketFileServiceImpl implements BucketFileService {
    private final FileService service;
    private final FileProperties fileProperties;

    private <R> R bucketHandler(@Nonnull final Function<String, R> handler) {
        final String bucket;
        Assert.hasText(bucket = fileProperties.getBucket(), "'bucket'未配置");
        return handler.apply(bucket);
    }

    @Override
    public boolean checkFileExists(@Nonnull final String key) {
        return bucketHandler(bucket -> service.checkFileExists(bucket, key));
    }

    @Override
    public DirectVO directUploadSignature(@Nonnull final DirectDTO dto) {
        return bucketHandler(bucket -> {
            dto.setBucket(bucket);
            return service.directUploadSignature(dto);
        });
    }

    @Override
    public FileVO upload(@Nonnull final String key, @Nonnull final String fileName, @Nonnull final InputStream input) {
        return bucketHandler(bucket -> service.upload(bucket, key, fileName, input));
    }

    @Override
    public InputStream getFileStream(@Nonnull final String key) {
        return bucketHandler(bucket -> service.getFileStream(bucket, key));
    }

    @Override
    public void copyFile(@Nonnull final String sourceKey, @Nonnull final String targetKey) {
        bucketHandler(bucket -> {
            service.copyFile(bucket, sourceKey, bucket, targetKey);
            return null;
        });
    }

    @Override
    public void deleteFiles(@Nonnull final String... keys) {
        bucketHandler(bucket -> {
            service.deleteFiles(bucket, keys);
            return null;
        });
    }
}
