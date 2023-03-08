package top.zenyoung.file.aliyun;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import top.zenyoung.file.FileService;
import top.zenyoung.file.FileServiceFactory;
import top.zenyoung.file.Properties;

import javax.annotation.Nonnull;

/**
 * 文件工厂(阿里云)
 *
 * @author young
 */
public class AliyunFileServiceFactory implements FileServiceFactory {

    @Nonnull
    @Override
    public String getType() {
        return AliyunConstants.TYPE;
    }

    @Override
    public FileService create(@Nonnull final Properties prop) {
        final CredentialsProvider provider = new DefaultCredentialProvider(prop.getAccessKeyId(), prop.getAccessKeySecret());
        final OSSClient client = new OSSClient(prop.getEndpoint(), provider, new ClientBuilderConfiguration());
        return AliyunFileService.of(client, prop.getExtHeaders());
    }
}
