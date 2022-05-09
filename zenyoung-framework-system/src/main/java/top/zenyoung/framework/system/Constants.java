package top.zenyoung.framework.system;

/**
 * 常量
 *
 * @author young
 */
public interface Constants {
    /**
     * 缓存分割符
     */
    String CACHE_SEP = ":";
    /**
     * 缓存前缀
     */
    String CACHE_PREFIX = "cache-system" + CACHE_SEP;
    /**
     * 缓存有效期
     */
    int CACHE_EXPIRE = 300;
}
