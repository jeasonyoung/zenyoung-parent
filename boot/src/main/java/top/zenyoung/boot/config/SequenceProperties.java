package top.zenyoung.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IdSequence 配置
 *
 * @author young
 */
@ConfigurationProperties("top.zenyoung.id-sequence")
public class SequenceProperties extends top.zenyoung.common.sequence.IdSequenceProperties {

}
