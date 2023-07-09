package top.zenyoung.file.huawei;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.obs.services.ObsClient;
import com.obs.services.model.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 文件-服务接口实现(华为云)
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class HuaweiFileService implements FileService {
    private static final long MAX_FILE_SIZE = 1048576000L;
    private final ObsClient client;
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
        final long d = Math.max(Objects.isNull(dto.getDuration()) ? 0 : dto.getDuration().toMillis(), 10000);
        final long expire = System.currentTimeMillis() + d;
        final Date expiration = new Date(expire);
        //
        final String dir = (Strings.isNullOrEmpty(dto.getDir()) ? "" : TrimUtils.trimPathSuffix(dto.getDir())) + TrimUtils.PATH_SEP;
        final String callback = buildCallbackBody(dto.getUploadId(), dto.getCallbackUrl());
        //上传策略设置
        final PostSignatureRequest signatureRequest = new PostSignatureRequest();
        signatureRequest.setBucketName(dto.getBucket());
        signatureRequest.setExpires(expire);
        signatureRequest.setExpiryDate(expiration);
        //
        final List<String> conditions = Lists.newLinkedList();
        //bucket
        conditions.add("{\"bucket\":\"" + dto.getBucket() + "\"}");
        //文件大小
        conditions.add("[\"content-length-range\", 0, " + MAX_FILE_SIZE + "]");
        //上传目录
        conditions.add("[\"starts-with\", \"$key\", \"" + dir + "\"]");
        //content-type
        conditions.add("[\"starts-with\",\"$content-type\",\"\"]");
        //返回状态
        conditions.add("[\"starts-with\",\"$success_action_status\",\"\"]");
        if (!Strings.isNullOrEmpty(callback)) {
            conditions.add("[\"starts-with\", \"$success_action_redirect\",\"" + callback + "\"]");
        }
        signatureRequest.setConditions(conditions);
        final PostSignatureResponse signatureRes = client.createPostSignature(signatureRequest);
        return DirectVO.builder()
                .type(HuaweiConstants.TYPE).accessId(dto.getAccessKeyId()).host(dto.getHost())
                .policy(signatureRes.getPolicy()).signature(signatureRes.getSignature()).expire(expire)
                .callback(callback)
                .dir(dir)
                .build();
    }

    private String buildCallbackBody(@Nonnull final String uploadId, @Nullable final String callbackUrl) {
        if (!Strings.isNullOrEmpty(uploadId) && !Strings.isNullOrEmpty(callbackUrl)) {
            final String sep = "?";
            return TrimUtils.trimPrefix(callbackUrl, sep) + sep + "uploadId=" + uploadId;
        }
        return null;
    }

    @Override
    @SneakyThrows({IOException.class})
    public FileVO upload(@Nonnull final String bucket, @Nonnull final String key, @Nonnull final String fileName, @Nonnull final InputStream input) {
        final long size = input.available();
        final ObjectMetadata metadata = createMetadata(fileName, size);
        final String ext = FilenameUtils.getExtension(key);
        ExtHeadersUtils.handler(extHeaders, ext, metadata::addUserMetadata);
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
            final ObsObject obj = client.getObject(bucket, key);
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
        ExtHeadersUtils.handler(extHeaders, ext, metadata::addUserMetadata);
        copyReq.setNewObjectMetadata(metadata);
        //复制文件
        final CopyObjectResult ret = client.copyObject(copyReq);
        log.info("copyFile(s-bucket: {},s-key: {}, t-bucket: {},t-key: {})=> {}", sourceBucket, sourceKey, targetBucket, targetKey, ret);
    }

    @Override
    public void deleteFiles(@Nonnull final String bucket, @Nonnull final String... keys) {
        if (keys.length == 0) {
            return;
        }
        if (keys.length == 1) {
            client.deleteObject(bucket, keys[0]);
            return;
        }
        final DeleteObjectsRequest delReq = new DeleteObjectsRequest(bucket);
        Stream.of(keys)
                .filter(key -> !Strings.isNullOrEmpty(key))
                .forEach(delReq::addKeyAndVersion);
        final DeleteObjectsResult ret = client.deleteObjects(delReq);
        log.info("deleteFiles(bucket: {},keys: {})=> {}", bucket, keys, ret);
    }
}
