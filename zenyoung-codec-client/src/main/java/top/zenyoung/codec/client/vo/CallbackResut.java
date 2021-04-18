package top.zenyoung.codec.client.vo;

import top.zenyoung.common.model.EnumData;

import java.util.List;

/**
 * 回调数据
 *
 * @author young
 */
public interface CallbackResut {

    /**
     * 获取上传ID
     *
     * @return 上传ID
     */
    String getId();

    /**
     * 获取上传业务ID
     *
     * @return 上传业务ID
     */
    String getBizId();

    /**
     * 获取文件唯一标识码
     *
     * @return 文件唯一标识码
     */
    String getUniqueCode();

    /**
     * 获取状态
     *
     * @return 状态
     */
    EnumData getStatus();

    /**
     * 获取消息
     *
     * @return 消息
     */
    String getMsg();

    /**
     * 获取转码地址集合
     *
     * @return 转码地址集合
     */
    List<CallbackCodecUrl> getUrls();
}
