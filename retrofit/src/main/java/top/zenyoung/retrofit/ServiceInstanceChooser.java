package top.zenyoung.retrofit;

import top.zenyoung.retrofit.exception.ServiceInstanceChooseException;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * 服务实例选择器
 *
 * @author young
 */
@FunctionalInterface
public interface ServiceInstanceChooser {
    /**
     * Chooses a ServiceInstance URI from the LoadBalancer for the specified service.
     *
     * @param serviceId The service ID to look up the LoadBalancer.
     * @return Return the uri of ServiceInstance
     */
    URI choose(@Nonnull final String serviceId);

    class NoValidServiceInstanceChooser implements ServiceInstanceChooser {

        @Override
        public URI choose(@Nonnull final String serviceId) {
            throw new ServiceInstanceChooseException("No valid service instance selector, Please configure it.");
        }
    }
}
