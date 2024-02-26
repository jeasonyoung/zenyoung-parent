package top.zenyoung.netty.server.config;

import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.config.BaseProperties;

import javax.annotation.Nonnull;
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
    public final boolean checkBlackList(@Nonnull final String ipAddr) {
        if (Strings.isNullOrEmpty(ipAddr) && CollectionUtils.isEmpty(getIpAddrBlackList())) {
            return false;
        }
        return getIpAddrBlackList().contains(ipAddr);
    }

    /**
     * 添加IP地址到黑名单
     *
     * @param ipAddr IP地址
     * @return 添加结果
     */
    public final boolean addBlackList(@Nonnull final String ipAddr) {
        if (Strings.isNullOrEmpty(ipAddr) || Objects.isNull(getIpAddrBlackList())) {
            return false;
        }
        if (!getIpAddrBlackList().contains(ipAddr)) {
            getIpAddrBlackList().add(ipAddr);
        }
        return true;
    }

    /**
     * 从黑名单中移除IP地址
     *
     * @param ipAddr IP地址
     * @return 移除结果
     */
    public final boolean removeBlackList(@Nonnull final String ipAddr) {
        if (Strings.isNullOrEmpty(ipAddr) || Objects.isNull(getIpAddrBlackList())) {
            return false;
        }
        getIpAddrBlackList().remove(ipAddr);
        return true;
    }
}
