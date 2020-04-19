package top.zenyoung.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 新增-结果
 *
 * @author yangyong
 * @version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddResult implements Serializable {
    /**
     * 新增主键ID
     */
    private String id;
}
