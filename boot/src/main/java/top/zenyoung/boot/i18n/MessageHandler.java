package top.zenyoung.boot.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

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
     * @param code
     * @return
     */
    public String getMessage(final String code) {
        return getMessage(code, null);
    }

    /**
     * 取国际化资源消息
     *
     * @param code
     * @param objects
     * @return
     */
    public String getMessage(final String code, final String[] objects) {
        return messageSource.getMessage(code, objects, LocaleContextHolder.getLocale());
    }
}
