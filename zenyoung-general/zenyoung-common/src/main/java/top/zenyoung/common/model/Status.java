package top.zenyoung.common.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 状态-枚举
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/5 4:27 下午
 **/
@Getter
@ToString
public enum Status implements EnumValue {
    /**
     * 删除
     */
    Del(-1, "删除"),
    /**
     * 停用
     */
    Disable(0, "停用"),
    /**
     * 启用
     */
    Enable(1, "启用");

    private final int val;
    private final String title;

    Status(final int val, final String title) {
        this.val = val;
        this.title = title;
    }

    public static Status parse(final Integer val) {
        if (val != null) {
            for (Status s : Status.values()) {
                if (s.getVal() == val) {
                    return s;
                }
            }
        }
        return null;
    }

    public static List<Status> toNormalAll() {
        return Stream.of(Status.values())
                .filter(s -> s.getVal() > -1)
                .sorted(Comparator.comparingInt(Status::getVal))
                .collect(Collectors.toList());
    }
}
