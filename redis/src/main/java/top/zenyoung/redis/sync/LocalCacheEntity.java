package top.zenyoung.redis.sync;

import com.alicp.jetcache.support.CacheMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 本地缓存实体
 *
 * @author young
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class LocalCacheEntity implements Serializable {
    /**
     * 发布者ID
     */
    private String issueId;
    /**
     * 缓存区域
     */
    private String area;
    /**
     * 缓存名称
     */
    private String cacheName;
    /**
     * 缓存消息
     */
    private CacheMessage cacheMessage;
}
