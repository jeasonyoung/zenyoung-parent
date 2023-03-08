package top.zenyoung.file.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import top.zenyoung.file.FileService;
import top.zenyoung.file.dto.DirectDTO;
import top.zenyoung.file.util.ExtHeadersUtils;
import top.zenyoung.file.util.TrimUtils;
import top.zenyoung.file.vo.DirectVO;
import top.zenyoung.file.vo.FileVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 文件-服务接口实现(阿里云)
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class AliyunFileService implements FileService {
    private final static long MAX_FILE_SIZE = 1048576000L;
    private final OSS client;
    private final Map<String, Map<String, String>> extHeaders;

    @Override
    public boolean checkFileExists(@Nonnull final String bucket, @Nonnull final String key) {
        if (!Strings.isNullOrEmpty(bucket) && !Strings.isNullOrEmpty(key)) {
            return client.doesObjectExist(bucket, key);
        }
        return false;
    }

    @Override
    public DirectVO directUploadSignature(@Nonnull final DirectDTO dto) {
        //上传有效期
        final long d = Math.max(Objects.isNull(dto.getDuration()) ? 0 : dto.getDuration().toMillis(), 10 * 1000);
        final long expire = System.currentTimeMillis() + d;
        final Date expiration = new Date(expire);
        //
        final PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, MAX_FILE_SIZE);
        final String dir = (Strings.isNullOrEmpty(dto.getDir()) ? "" : TrimUtils.trimPathSuffix(dto.getDir())) + TrimUtils.PATH_SEP;
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
        //
        final String postPolicy = client.generatePostPolicy(expiration, policyConds);
        final byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
        final String encodedPolicy = Base64.encodeBase64String(binaryData);
        final String postSignature = client.calculatePostSignature(postPolicy);
        final String callbackBody = buildCallbackBody(dto.getUploadId(), dto.getCallbackUrl());
        return DirectVO.builder()
                .type(AliyunConstants.TYPE).accessId(dto.getAccessKeyId()).host(dto.getHost())
                .policy(encodedPolicy).signature(postSignature).expire(expire)
                .callback(callbackBody)
                .dir(dir)
                .build();
    }

    private String buildCallbackBody(@Nonnull final String uploadId, @Nullable final String callbackUrl) {
        if (!Strings.isNullOrEmpty(callbackUrl)) {
            final String callbackBody = "{\"callbackUrl\":\"" + callbackUrl + "\"," +
                    "\"callbackBody\":{" +
                    "\"uploadId\":\"" + uploadId + "\"," +
                    "\"filename\":\"${object}\"," +
                    "\"size\":\"${size}\"," +
                    "\"mimeType\":\"${mimeType}\"," +
                    "\"height\":\"${imageInfo.height}\"," +
                    "\"width\":\"${imageInfo.width}\"" +
                    "}," +
                    "\"callbackBodyType\":\"application/json\"" +
                    "}";
            final byte[] raw = callbackBody.getBytes(StandardCharsets.UTF_8);
            return Base64.encodeBase64String(raw);
        }
        return null;
    }

    @Override
    @SneakyThrows({IOException.class})
    public FileVO upload(@Nonnull final String bucket, @Nonnull final String key, @Nonnull final String fileName, @Nonnull final InputStream input) {
        //文件大小
        final long size = input.available();
        //后缀名
        final String ext = FilenameUtils.getExtension(key);
        //创建上传配置
        final ObjectMetadata metadata = createMetadata(fileName, size);
        ExtHeadersUtils.handler(extHeaders, ext, metadata::setHeader);
        //上传文件
        final PutObjectResult ret = client.putObject(bucket, key, input, metadata);
        log.info("upload(bucket: {},key: {})=> {}", bucket, key, ret);
        //返回文件
        return FileVO.builder()
                .name(fileName)
                .size(size)
                .suffix(ext)
                .key(key)
                .build();
    }

    private ObjectMetadata createMetadata(@Nonnull final String fileName, @Nullable final Long contentLen) {
        final ObjectMetadata metadata = new ObjectMetadata();
        if (Objects.nonNull(contentLen) && contentLen > 0) {
            metadata.setContentLength(contentLen);
        }
        metadata.setContentEncoding("utf-8");
        metadata.setCacheControl("no-cache");
        metadata.setHeader("Pragma", "no-cache");
        //设置Content-disposition的内容模板格式，基于base64编码格式
        final String fileNameTemplate = "inline;filename=\"=?UTF8?B?%s?=\"";
        //对真正文件名称进行base64编码
        final String base64FileName = Base64.encodeBase64String(fileName.getBytes(StandardCharsets.UTF_8));
        metadata.setContentDisposition(String.format(fileNameTemplate, base64FileName));
        //
        return metadata;
    }

    @Override
    public InputStream getFileStream(@Nonnull final String bucket, @Nonnull final String key) {
        if (!Strings.isNullOrEmpty(bucket) && !Strings.isNullOrEmpty(key)) {
            final OSSObject obj = client.getObject(bucket, key);
            if (Objects.nonNull(obj)) {
                return obj.getObjectContent();
            }
        }
        return null;
    }

    @Override
    public void copyFile(@Nonnull final String sourceBucket, @Nonnull final String sourceKey,
                         @Nonnull final String targetBucket, @Nonnull final String targetKey) {
        final CopyObjectRequest copyReq = new CopyObjectRequest(sourceBucket, sourceKey, targetBucket, targetKey);
        //设置新的文件元信息
        final ObjectMetadata metadata = client.getObjectMetadata(sourceBucket, sourceKey);
        final String ext = FilenameUtils.getExtension(targetKey);
        ExtHeadersUtils.handler(extHeaders, ext, metadata::setHeader);
        copyReq.setNewObjectMetadata(metadata);
        //复制文件
        final CopyObjectResult ret = client.copyObject(copyReq);
        log.info("copyFile(s-bucket: {},s-key: {}, t-bucket: {},t-key: {})=> {}", sourceBucket, sourceKey, targetBucket, targetKey, ret);
    }

    @Override
    public void deleteFiles(@Nonnull final String bucket, @Nonnull final String... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return;
        }
        if (keys.length == 1) {
            client.deleteObject(bucket, keys[0]);
            return;
        }
        final DeleteObjectsRequest delReq = new DeleteObjectsRequest(bucket);
        delReq.withKeys(Lists.newArrayList(keys));
        final DeleteObjectsResult ret = client.deleteObjects(delReq);
        log.info("deleteFiles(bucket: {},keys: {})=> {}", bucket, keys, ret);
    }
}
