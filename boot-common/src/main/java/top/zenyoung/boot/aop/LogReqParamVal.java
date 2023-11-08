package top.zenyoung.boot.aop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class LogReqParamVal {
    /**
     * 参数标题
     */
    private String title;
    /**
     * 参数值
     */
    private Object val;
}
