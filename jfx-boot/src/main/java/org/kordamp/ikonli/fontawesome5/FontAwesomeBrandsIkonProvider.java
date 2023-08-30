package org.kordamp.ikonli.fontawesome5;

import org.kordamp.ikonli.IkonProvider;
import org.kordamp.jipsy.ServiceProviderFor;

/**
 * FontAwesomeBrandsIkonProvider
 *
 * @author young
 */
@ServiceProviderFor(IkonProvider.class)
public class FontAwesomeBrandsIkonProvider implements IkonProvider<FontAwesomeBrands> {

    @Override
    public Class<FontAwesomeBrands> getIkon() {
        return FontAwesomeBrands.class;
    }
}
