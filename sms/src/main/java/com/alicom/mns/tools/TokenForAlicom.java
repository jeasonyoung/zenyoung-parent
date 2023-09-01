package com.alicom.mns.tools;

import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.MNSClient;
import lombok.Data;

/**
 * TokenForAlicom
 *
 * @author aliyun
 */
@Data
public class TokenForAlicom {
    private String messageType;
    private String token;
    private Long expireTime;
    private String tempAccessKeyId;
    private String tempAccessKeySecret;
    private MNSClient client;
    private CloudQueue queue;

    public void closeClient() {
        if (this.client != null) {
            this.client.close();
        }
    }
}
