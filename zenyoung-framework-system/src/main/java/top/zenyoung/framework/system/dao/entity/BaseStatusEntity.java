package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import top.zenyoung.common.model.Status;
import top.zenyoung.data.converter.StatusConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;

/**
 * 数据实体(状态)-基类
 *
 * @author yangyong
 * @version 1.0
 * 2020/2/6 10:27 上午
 **/
@Getter
@Setter
@ToString
@MappedSuperclass
class BaseStatusEntity extends BaseEntity {

    /**
     * 状态(-1:删除,0:停用,1:启用)
     */
    @Column(nullable = false)
    @Convert(converter = StatusConverter.class)
    private Status status = Status.Enable;
}
