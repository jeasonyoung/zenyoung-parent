package org.kordamp.ikonli.fontawesome5;

import org.kordamp.ikonli.IkonProvider;
import org.kordamp.jipsy.ServiceProviderFor;

/**
 * FontAwesomeSolidIkonProvider
 *
 * @author young
 */
@ServiceProviderFor(IkonProvider.class)
public class FontAwesomeSolidIkonProvider implements IkonProvider<FontAwesomeSolid> {

    @Override
    public Class<FontAwesomeSolid> getIkon() {
        return FontAwesomeSolid.class;
    }
}
