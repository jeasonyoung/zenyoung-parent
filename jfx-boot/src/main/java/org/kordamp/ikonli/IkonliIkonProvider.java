package org.kordamp.ikonli;

import org.kordamp.jipsy.ServiceProviderFor;

/**
 * IkonliIkonProvider
 *
 * @author young
 */
@ServiceProviderFor(IkonProvider.class)
public class IkonliIkonProvider implements IkonProvider<Ikonli> {

    @Override
    public Class<Ikonli> getIkon() {
        return Ikonli.class;
    }
}
