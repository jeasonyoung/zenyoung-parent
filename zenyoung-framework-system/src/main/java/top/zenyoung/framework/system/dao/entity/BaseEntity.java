package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 数据实体-基类
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/6 10:23 上午
 **/
@Getter
@Setter
@ToString
@MappedSuperclass
abstract class BaseEntity implements Serializable {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(generator = "snowFlake")
    @GenericGenerator(name = "snowFlake", strategy = "top.zenyoung.data.generator.SnowFlakeIdentityGenerator")
    @Column(unique = true, nullable = false)
    private Long id;

    /**
     * 创建时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime = new Date();

    /**
     * 更新时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    private Date updateTime = new Date();
}
