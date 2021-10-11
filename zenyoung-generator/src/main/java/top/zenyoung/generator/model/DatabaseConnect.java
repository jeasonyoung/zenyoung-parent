package top.zenyoung.generator.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据库链接
 * @author young
 */
@Data
public class DatabaseConnect implements Serializable {
    /**
     * 数据库链接字符串
     */
    private String connectString;
    /**
     * 数据库账号
     */
    private String account;
    /**
     * 数据库密码
     */
    private String passwd;
}
