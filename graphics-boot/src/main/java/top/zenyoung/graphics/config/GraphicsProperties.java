package top.zenyoung.graphics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置属性
 *
 * @author young
 */
@Data
@ConfigurationProperties(prefix = "top.zenyoung.graphics")
public class GraphicsProperties {

}
