package org.kordamp.ikonli;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * IkonResolver-抽象类
 *
 * @author young
 */
@Slf4j
public class AbstractIkonResolver {
    private static final String ORG_KORDAMP_IKONLI_STRICT = "org.kordamp.ikonli.strict";

    protected boolean registerHandler(@Nonnull final IkonHandler handler, @Nonnull final Set<IkonHandler> handlers,
                                      @Nonnull final Set<IkonHandler> customHandlers) {
        // check whether handler for this font is already loaded via classpath
        if (isLoadedViaClasspath(handler, handlers)) {
            throwOrWarn(String.format("IkonHandler for %s is already loaded via classpath", handler.getFontFamily()));
            return false;
        }
        return customHandlers.add(handler);
    }

    protected boolean unregisterHandler(@Nonnull final IkonHandler handler, @Nonnull final Set<IkonHandler> handlers,
                                        @Nonnull final Set<IkonHandler> customHandlers) {
        // check whether handler for this font is loaded via classpath
        if (isLoadedViaClasspath(handler, handlers)) {
            throwOrWarn(String.format("IkonHandler for %s was loaded via classpath and can't be unregistered", handler.getFontFamily()));
            return false;
        }
        return customHandlers.remove(handler);
    }

    protected IkonHandler resolve(String value, Set<IkonHandler> handlers, Set<IkonHandler> customHandlers) {
        requireNonNull(value, "Ikon description must not be null");
        for (Set<IkonHandler> hs : Arrays.asList(handlers, customHandlers)) {
            for (IkonHandler handler : hs) {
                if (handler.supports(value)) {
                    return handler;
                }
            }
        }
        throw new UnsupportedOperationException("Cannot resolve '" + value + "'");
    }

    private boolean isLoadedViaClasspath(@Nonnull final IkonHandler handler, @Nonnull final Set<IkonHandler> handlers) {
        final String fontFamily = handler.getFontFamily();
        for (final IkonHandler classpathHandler : handlers) {
            if (classpathHandler.getFontFamily().equals(fontFamily)) {
                return true;
            }
        }
        return false;
    }

    private void throwOrWarn(String message) {
        if (strictChecksEnabled()) {
            throw new IllegalArgumentException(message);
        } else {
            log.warn(message);
        }
    }

    private boolean strictChecksEnabled() {
        return System.getProperty(ORG_KORDAMP_IKONLI_STRICT) == null || Boolean.getBoolean(ORG_KORDAMP_IKONLI_STRICT);
    }

    public static ServiceLoader<IkonHandler> resolveServiceLoader() {
        //Check if the IkonHandler.class.classLoader works
        final ServiceLoader<IkonHandler> handlers = ServiceLoader.load(IkonHandler.class, IkonHandler.class.getClassLoader());
        final Iterator<IkonHandler> iterator = handlers.iterator();
        if (iterator.hasNext()) {
            return handlers;
        }
        // If *nothing* else works
        return ServiceLoader.load(IkonHandler.class);
    }
}
