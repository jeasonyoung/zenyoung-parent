package top.zenyoung.boot.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 国际消息处理器
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class MessageHandler {
    private final MessageSource messageSource;

    /**
     * 取国际化资源消息
     *
     * @param code 资源Code
     * @return 资源数据
     */
    public String getMessage(@Nonnull final String code) {
        return getMessage(code, null);
    }

    /**
     * 取国际化资源消息
     *
     * @param code    资源Code
     * @param objects 资源对象
     * @return 资源消息
     */
    public String getMessage(@Nonnull final String code, @Nullable final String[] objects) {
        return messageSource.getMessage(code, objects, LocaleContextHolder.getLocale());
    }
}
