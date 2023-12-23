package top.zenyoung.boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * IdSequence 配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung.id-sequence")
public class SequenceProperties implements Serializable {
    /**
     * 机器ID
     */
    private Long workerId;
    /**
     * 机房ID
     */
    private Long dataCenterId;
    /**
     * 顺序号
     */
    private Long sequence;
}
