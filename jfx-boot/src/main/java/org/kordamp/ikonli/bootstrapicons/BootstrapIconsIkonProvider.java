package org.kordamp.ikonli.bootstrapicons;

import org.kordamp.ikonli.IkonProvider;
import org.kordamp.jipsy.ServiceProviderFor;

/**
 * BootstrapIconsIkonProvider
 *
 * @author young
 */
@ServiceProviderFor(IkonProvider.class)
public class BootstrapIconsIkonProvider implements IkonProvider<BootstrapIcons> {

    @Override
    public Class<BootstrapIcons> getIkon() {
        return BootstrapIcons.class;
    }
}
