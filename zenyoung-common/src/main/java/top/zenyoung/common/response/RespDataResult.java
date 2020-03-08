package top.zenyoung.common.response;

import java.io.Serializable;

/**
 * 响应数据结果
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/2 5:52 下午
 **/
public class RespDataResult<T extends Serializable> extends RespResult<DataResult<T>> {
    /**
     * 构造函数
     */
    public RespDataResult() {
        buildResult(ResultCode.Success);
    }
}
