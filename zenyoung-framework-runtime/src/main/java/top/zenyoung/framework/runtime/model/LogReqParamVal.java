package top.zenyoung.framework.runtime.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 操作日志-请求参数
 *
 * @author young
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class LogReqParamVal implements Serializable {
    /**
     * 参数标题
     */
    private String title;
    /**
     * 参数值
     */
    private Object val;
}
