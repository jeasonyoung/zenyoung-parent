package top.zenyoung.segment.exception;

import lombok.Getter;

/**
 * 未找到最大ID异常
 *
 * @author young
 */
@Getter
public class NotFoundMaxIdException extends SegmentException {
    private final String name;

    public NotFoundMaxIdException(final String name){
        super(name + ":not found max id ");
        this.name = name;
    }
}
