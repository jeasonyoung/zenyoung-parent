package org.kordamp.ikonli.fontawesome5;

import org.kordamp.ikonli.IkonProvider;
import org.kordamp.jipsy.ServiceProviderFor;

/**
 * FontAwesomeRegularIkonProvider
 *
 * @author young
 */
@ServiceProviderFor(IkonProvider.class)
public class FontAwesomeRegularIkonProvider implements IkonProvider<FontAwesomeRegular> {

    @Override
    public Class<FontAwesomeRegular> getIkon() {
        return FontAwesomeRegular.class;
    }
}
