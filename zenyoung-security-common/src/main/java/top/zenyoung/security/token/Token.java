package top.zenyoung.security.token;

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
     * 授权令牌
     */
    private String token;
    /**
     * 刷新令牌
     */
    private String refershToken;
}
