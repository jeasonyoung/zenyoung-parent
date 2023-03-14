package top.zenyoung.file.huawei;

import com.obs.services.ObsClient;
import top.zenyoung.file.FileService;
import top.zenyoung.file.FileServiceFactory;
import top.zenyoung.file.FileProperties;

import javax.annotation.Nonnull;

/**
 * 文件工厂(华为云)
 *
 * @author young
 */
public class HuaweiFileServiceFactory implements FileServiceFactory {
    @Nonnull
    @Override
    public String getType() {
        return HuaweiConstants.TYPE;
    }

    @Override
    public FileService create(@Nonnull final FileProperties prop) {
        final ObsClient client = new ObsClient(prop.getAccessKeyId(), prop.getAccessKeySecret(), prop.getEndpoint());
        return HuaweiFileService.of(client, prop.getExtHeaders());
    }
}
