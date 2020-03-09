package top.zenyoung.code.generator.common;

import lombok.Getter;

import javax.annotation.Nullable;

/**
 * 代码生成类型
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/9 12:57 下午
 **/
@Getter
public enum CodeType {
    /**
     * 未定义
     */
    None(0, "未定义"),
    /**
     * JPA
     */
    Jpa(1, "JPA"),
    /**
     * MyBatis
     */
    MyBatis(2, "MyBatis"),
    /**
     * 数据层
     */
    Repository(4, "Repository"),
    /**
     * 业务层
     */
    Service(8, "Service"),
    /**
     * Mvc
     */
    Mvc(16, "MVC"),
    /**
     * WebFlux
     */
    WebFlux(32, "WebFlux");

    private final int val;
    private final String title;

    CodeType(final int val, final String title) {
        this.val = val;
        this.title = title;
    }

    public static CodeType parse(@Nullable final Integer val) {
        if (val != null) {
            for (CodeType t : CodeType.values()) {
                if (t.getVal() == val) {
                    return t;
                }
            }
        }
        return None;
    }
}
