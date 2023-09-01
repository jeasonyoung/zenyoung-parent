package org.kordamp.bootstrapfx;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.Optional;

/**
 * Bootstrap FX
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BootstrapFX {

    public static String bootstrapFXStylesheet() {
        return Optional.of(BootstrapFX.class)
                .map(cls -> cls.getResource("bootstrapfx.css"))
                .map(URL::toExternalForm)
                .orElse(null);
    }
}
