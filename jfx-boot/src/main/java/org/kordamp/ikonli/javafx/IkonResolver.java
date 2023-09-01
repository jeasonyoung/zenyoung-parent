package org.kordamp.ikonli.javafx;

import com.google.common.collect.Sets;
import javafx.scene.text.Font;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.kordamp.ikonli.AbstractIkonResolver;
import org.kordamp.ikonli.IkonHandler;

import javax.annotation.Nonnull;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * IkonResolver
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IkonResolver extends AbstractIkonResolver {
    private static final IkonResolver INSTANCE;
    private static final Set<IkonHandler> HANDLERS = Sets.newLinkedHashSet();
    private static final Set<IkonHandler> CUSTOM_HANDLERS = Sets.newLinkedHashSet();

    static {
        INSTANCE = new IkonResolver();
        final ServiceLoader<IkonHandler> loader = resolveServiceLoader();
        for (final IkonHandler handler : loader) {
            HANDLERS.add(handler);
            handler.setFont(Font.loadFont(handler.getFontResource().toExternalForm(), 16));
        }
    }

    /**
     * 检查是否已被注册
     *
     * @param handler IkonHandler
     * @return true if the specified handler was not already registered.
     */
    public boolean registerHandler(@Nonnull final IkonHandler handler) {
        return registerHandler(handler, HANDLERS, CUSTOM_HANDLERS);
    }

    /**
     * 检查是否已移除注册
     *
     * @param handler IkonHandler
     * @return true if the specified handler was removed from the set of handlers。
     */
    public boolean unregisterHandler(@Nonnull final IkonHandler handler) {
        return unregisterHandler(handler, HANDLERS, CUSTOM_HANDLERS);
    }

    public IkonHandler resolve(@Nonnull final String value) {
        return resolve(value, HANDLERS, CUSTOM_HANDLERS);
    }

    public static IkonResolver getInstance() {
        return INSTANCE;
    }
}
