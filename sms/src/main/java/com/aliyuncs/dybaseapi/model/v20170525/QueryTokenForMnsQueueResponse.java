package com.aliyuncs.dybaseapi.model.v20170525;

import com.aliyuncs.AcsResponse;
import com.aliyuncs.dybaseapi.transform.v20170525.QueryTokenForMnsQueueResponseUnmarshaller;
import com.aliyuncs.transform.UnmarshallerContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MnsQueue
 *
 * @author aliyun
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryTokenForMnsQueueResponse extends AcsResponse {
    private String requestId;
    private String code;
    private String message;
    private MessageTokenDTO messageTokenDTO;

    @Override
    public AcsResponse getInstance(final UnmarshallerContext context) {
        return QueryTokenForMnsQueueResponseUnmarshaller.unmarshall(this, context);
    }

    @Data
    public static class MessageTokenDTO {
        private String accessKeyId;
        private String accessKeySecret;
        private String securityToken;
        private String createTime;
        private String expireTime;
    }
}
