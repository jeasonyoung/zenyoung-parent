package org.kordamp.ikonli.bootstrapicons;

import com.google.common.base.Strings;
import org.kordamp.ikonli.AbstractIkonHandler;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import org.kordamp.jipsy.ServiceProviderFor;

import java.io.InputStream;
import java.net.URL;

/**
 * BootstrapIconsIkonHandler
 *
 * @author young
 */
@ServiceProviderFor(IkonHandler.class)
public class BootstrapIconsIkonHandler extends AbstractIkonHandler {
    private static final String FONT_RESOURCE = "/META-INF/bootstrapicons/1.3.0/fonts/bootstrap-icons.ttf";

    @Override
    public boolean supports(final String description) {
        return !Strings.isNullOrEmpty(description) && description.startsWith("bi-");
    }

    @Override
    public Ikon resolve(final String description) {
        return BootstrapIcons.findByDescription(description);
    }

    @Override
    public URL getFontResource() {
        return getClass().getResource(FONT_RESOURCE);
    }

    @Override
    public InputStream getFontResourceAsStream() {
        return getClass().getResourceAsStream(FONT_RESOURCE);
    }

    @Override
    public String getFontFamily() {
        return "bootstrap-icons";
    }
}