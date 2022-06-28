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
public class IdSequenceProperties implements Serializable {
    /**
     * 机器ID
     */
    private Integer workerId;
    /**
     * 机房ID
     */
    private Integer dataCenterId;
    /**
     * 顺序号
     */
    private Integer sequence;
}
