package top.zenyoung.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
@Schema(description = "状态")
@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Status implements EnumValue {
    /**
     * 停用
     */
    DISABLE(0, "停用"),
    /**
     * 启用
     */
    ENABLE(1, "启用");

    private final int val;
    private final String title;

    /**
     * 状态转换
     *
     * @param val 状态值
     * @return 状态对象
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
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
