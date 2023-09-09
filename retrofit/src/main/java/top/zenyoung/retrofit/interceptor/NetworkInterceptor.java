package top.zenyoung.retrofit.interceptor;

import okhttp3.Interceptor;

/**
 * 网络拦截器-接口
 *
 * @author young
 */
public interface NetworkInterceptor extends Interceptor {
    /**
     * 拦截器排序值
     *
     * @return 排序值
     */
    default int order() {
        return 0;
    }
}
