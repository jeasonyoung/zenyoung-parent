package top.zenyoung.common.model;

import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
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

    /**
     * 构造函数
     *
     * @param val   状态值
     * @param title 状态标题
     */
    Status(final int val, final String title) {
        this.val = val;
        this.title = title;
    }

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

    /**
     * 创建响应对象
     *
     * @param status 状态
     * @return 响应对象
     */
    public static EnumData createResp(@Nullable final Status status) {
        if (status != null) {
            return EnumData.parse(status);
        }
        return null;
    }
}
