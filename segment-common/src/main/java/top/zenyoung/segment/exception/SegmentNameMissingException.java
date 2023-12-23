package top.zenyoung.segment.exception;

import com.google.common.base.Strings;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * 分段名称丢失异常
 *
 * @author young
 */
@Getter
public class SegmentNameMissingException extends SegmentException {
    private final String name;

    public SegmentNameMissingException(@Nonnull final String name) {
        super(Strings.lenientFormat("name:[%s] missing.", name));
        this.name = name;
    }
}
