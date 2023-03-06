package top.zenyoung.file.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件VO
 *
 * @author young
 */
@Data
@Builder
public class FileVO implements Serializable {
    /**
     * 文件名称
     */
    private String name;
    /**
     * 文件键
     */
    private String key;
    /**
     * 文件大小
     */
    private long size;
    /**
     * 文件后缀
     */
    private String suffix;
}
