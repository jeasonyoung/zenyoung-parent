package top.zenyoung.orm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.boot.config.IdSequenceProperties;

/**
 * ORM 主键规则配置
 *
 * @author young
 */
@ConfigurationProperties("top.zenyoung.orm-id-sequence")
public class OrmIdSequenceProperties extends IdSequenceProperties {

}
