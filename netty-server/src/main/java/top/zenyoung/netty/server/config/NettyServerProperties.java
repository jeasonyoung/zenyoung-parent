package top.zenyoung.netty.server.config;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.config.BaseProperties;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * NettyServer-配置
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("top.zenyoung.netty.server")
public class NettyServerProperties extends BaseProperties {
    /**
     * 保持连接数(默认1024)
     */
    private Integer backlog = 1024;
    /**
     * IP地址黑名单
     */
    private List<String> ipAddrBlackList = Lists.newArrayList();
    /**
     * IP地址白名单
     */
    private List<String> ipAddrWhiteList = Lists.newArrayList();
    /**
     * 请求访问限制
     */
    private RequestLimit limit = new RequestLimit();
    /**
     * 配置编解码器(支持多端口,多协议监听)
     * 端口号: (编解码器名称,编解码器类或Bean名)
     */
    private Map<Integer, Map<String, String>> codec;

    /**
     * 检查IP地址是否在黑名单中存在
     *
     * @param ipAddr IP地址
     * @return 是否存在
     */
    public boolean checkBlackList(@Nonnull final String ipAddr) {
        if (Strings.isNullOrEmpty(ipAddr) && CollectionUtils.isEmpty(this.ipAddrBlackList)) {
            return false;
        }
        return this.ipAddrBlackList.contains(ipAddr);
    }

    /**
     * 添加IP地址到黑名单
     *
     * @param ipAddr IP地址
     * @return 添加结果
     */
    public boolean addBlackList(@Nonnull final String ipAddr) {
        if (Strings.isNullOrEmpty(ipAddr) || Objects.isNull(this.ipAddrBlackList)) {
            return false;
        }
        if (!this.ipAddrBlackList.contains(ipAddr)) {
            this.ipAddrBlackList.add(ipAddr);
        }
        return true;
    }

    /**
     * 从黑名单中移除IP地址
     *
     * @param ipAddr IP地址
     * @return 移除结果
     */
    public boolean removeBlackList(@Nonnull final String ipAddr) {
        if (Strings.isNullOrEmpty(ipAddr) || Objects.isNull(this.ipAddrBlackList)) {
            return false;
        }
        this.ipAddrBlackList.remove(ipAddr);
        return true;
    }
}
