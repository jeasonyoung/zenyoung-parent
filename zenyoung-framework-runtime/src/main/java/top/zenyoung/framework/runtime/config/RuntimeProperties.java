package top.zenyoung.framework.runtime.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.framework.auth.AuthConfig;

/**
 * 运行时模块配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung")
public class RuntimeProperties {
    /**
     * 登录认证配置
     */
    private AuthConfig auth = new AuthConfig();
    /**
     * 防止重复提交
     */
    private RepeatSubmitConfig repeatSubmit = new RepeatSubmitConfig();
}
