package com.aliyuncs.dybaseapi.transform.v20170525;

import com.aliyuncs.dybaseapi.model.v20170525.QueryTokenForMnsQueueResponse;
import com.aliyuncs.transform.UnmarshallerContext;

/**
 * MnsQueueResponseUnmarshaller
 *
 * @author aliyun
 */
public class QueryTokenForMnsQueueResponseUnmarshaller {

    public static QueryTokenForMnsQueueResponse unmarshall(final QueryTokenForMnsQueueResponse res, final UnmarshallerContext context) {
        res.setRequestId(context.stringValue("QueryTokenForMnsQueueResponse.RequestId"));
        res.setCode(context.stringValue("QueryTokenForMnsQueueResponse.Code"));
        res.setMessage(context.stringValue("QueryTokenForMnsQueueResponse.Message"));
        final QueryTokenForMnsQueueResponse.MessageTokenDTO dto = new QueryTokenForMnsQueueResponse.MessageTokenDTO();
        dto.setAccessKeyId(context.stringValue("QueryTokenForMnsQueueResponse.MessageTokenDTO.AccessKeyId"));
        dto.setAccessKeySecret(context.stringValue("QueryTokenForMnsQueueResponse.MessageTokenDTO.AccessKeySecret"));
        dto.setSecurityToken(context.stringValue("QueryTokenForMnsQueueResponse.MessageTokenDTO.SecurityToken"));
        dto.setCreateTime(context.stringValue("QueryTokenForMnsQueueResponse.MessageTokenDTO.CreateTime"));
        dto.setExpireTime(context.stringValue("QueryTokenForMnsQueueResponse.MessageTokenDTO.ExpireTime"));
        res.setMessageTokenDTO(dto);
        return res;
    }
}
