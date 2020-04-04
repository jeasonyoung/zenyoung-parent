package top.zenyoung.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 异常处理器
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/7 12:32 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptHandler {
    /**
     * 响应代码
     */
    private Integer code;

    /**
     * 错误类型
     */
    private Class<? extends Throwable> eClass;
}
