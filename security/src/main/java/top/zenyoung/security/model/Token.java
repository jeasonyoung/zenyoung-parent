package top.zenyoung.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * token数据
 *
 * @author young
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Token implements Serializable {
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 刷新令牌
     */
    private String refershToken;
}
