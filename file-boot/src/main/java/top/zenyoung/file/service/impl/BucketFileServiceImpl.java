package top.zenyoung.file.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import top.zenyoung.file.FileService;
import top.zenyoung.file.config.FileStorageProperties;
import top.zenyoung.file.dto.DirectDTO;
import top.zenyoung.file.service.BucketFileService;
import top.zenyoung.file.vo.DirectVO;
import top.zenyoung.file.vo.FileVO;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.function.BiFunction;

/**
 * 配置文件桶-文件服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
public class BucketFileServiceImpl implements BucketFileService {
    @Autowired(required = false)
    private FileService service;
    @Autowired(required = false)
    private FileStorageProperties fileProperties;

    private <R> R handler(@Nonnull final BiFunction<FileService, String, R> handler) {
        Assert.notNull(fileProperties, "'fileProperties未注入");
        final String bucket;
        Assert.hasText(bucket = fileProperties.getBucket(), "'bucket'未配置");
        Assert.notNull(service, "'FileService'未注入");
        return handler.apply(service, bucket);
    }

    @Override
    public boolean checkFileExists(@Nonnull final String key) {
        return handler((srv, bucket) -> srv.checkFileExists(bucket, key));
    }

    @Override
    public DirectVO directUploadSignature(@Nonnull final DirectDTO dto) {
        return handler((srv, bucket) -> {
            dto.setBucket(bucket);
            return srv.directUploadSignature(dto);
        });
    }

    @Override
    public FileVO upload(@Nonnull final String key, @Nonnull final String fileName, @Nonnull final InputStream input) {
        return handler((srv, bucket) -> srv.upload(bucket, key, fileName, input));
    }

    @Override
    public InputStream getFileStream(@Nonnull final String key) {
        return handler((srv, bucket) -> srv.getFileStream(bucket, key));
    }

    @Override
    public void copyFile(@Nonnull final String sourceKey, @Nonnull final String targetKey) {
        handler((srv, bucket) -> {
            srv.copyFile(bucket, sourceKey, bucket, targetKey);
            return null;
        });
    }

    @Override
    public void deleteFiles(@Nonnull final String... keys) {
        handler((srv, bucket) -> {
            srv.deleteFiles(bucket, keys);
            return null;
        });
    }
}
