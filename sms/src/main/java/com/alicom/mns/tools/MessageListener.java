package com.alicom.mns.tools;

import com.aliyun.mns.model.Message;

/**
 * 消息监听
 *
 * @author aliyun
 */
public interface MessageListener {

    boolean dealMessage(final Message message);
}
