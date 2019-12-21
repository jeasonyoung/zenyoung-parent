package top.zenyoung.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 状态-枚举
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/10/19 10:25 下午
 **/
@Getter
@ToString
public enum Status implements Serializable {
    /**
     * 删除
     */
    Del(-1, "删除"),
    /**
     * 停用
     */
    Stop(0, "停用"),
    /**
     * 启动
     */
    Start(1, "启动");

    @JsonValue
    private final int val;
    private final String title;

    /**
     * 构造函数
     *
     * @param val   枚举值
     * @param title 枚举名称
     */
    Status(final int val, final String title) {
        this.val = val;
        this.title = title;
    }

    /**
     * 枚举解析
     *
     * @param val 枚举值
     * @return 枚举对象
     */
    @JsonCreator
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
}
