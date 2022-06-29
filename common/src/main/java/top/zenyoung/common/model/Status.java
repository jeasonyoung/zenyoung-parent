package top.zenyoung.common.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
 **/
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Status implements EnumValue {
    /**
     * 停用
     */
    Disable(0, "停用"),
    /**
     * 启用
     */
    Enable(1, "启用");

    @com.baomidou.mybatisplus.annotation.EnumValue
    private final int val;
    private final String title;

    /**
     * 状态转换
     *
     * @param val 状态值
     * @return 状态对象
     */
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

    /**
     * 获取非零状态集合
     *
     * @return 状态集合
     */
    public static List<Status> toNormalAll() {
        return Stream.of(Status.values())
                .filter(s -> s.getVal() > -1)
                .sorted(Comparator.comparingInt(Status::getVal))
                .collect(Collectors.toList());
    }
}
