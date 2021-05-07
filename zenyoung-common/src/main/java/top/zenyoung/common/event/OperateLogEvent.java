package top.zenyoung.common.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;

/**
 * 操作日志-事件数据
 *
 * @author young
 */
@Data
@RequiredArgsConstructor(staticName = "of")
public class OperateLogEvent implements Serializable {
    /**
     * 当前用户数据
     */
    private final Principal principal;
    /**
     * 操作描述
     */
    private final String operate;
    /**
     * 请求URI
     */
    private final String uri;
    /**
     * 操作方法
     */
    private final String method;
    /**
     * 参数数据
     */
    private final List<String> params;
    /**
     * 浏览器Agent数据
     */
    private final String agent;
    /**
     * 客户端IP地址
     */
    private final String clientIpAddr;
}
