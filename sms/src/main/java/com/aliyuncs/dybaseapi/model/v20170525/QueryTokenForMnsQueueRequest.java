package com.aliyuncs.dybaseapi.model.v20170525;

import com.aliyuncs.RpcAcsRequest;
import lombok.Getter;

/**
 * MnsQueueRequest
 *
 * @author aliyun
 */
@Getter
public class QueryTokenForMnsQueueRequest extends RpcAcsRequest<QueryTokenForMnsQueueResponse> {
    private String resourceOwnerAccount;
    private String messageType;
    private String queueName;
    private Long resourceOwnerId;
    private Long ownerId;

    public QueryTokenForMnsQueueRequest() {
        super("Dybaseapi", "2017-05-25", "QueryTokenForMnsQueue");
    }

    public void setResourceOwnerAccount(final String resourceOwnerAccount) {
        this.resourceOwnerAccount = resourceOwnerAccount;
        if (resourceOwnerAccount != null) {
            this.putQueryParameter("ResourceOwnerAccount", resourceOwnerAccount);
        }
    }

    public void setMessageType(final String messageType) {
        this.messageType = messageType;
        if (messageType != null) {
            this.putQueryParameter("MessageType", messageType);
        }
    }

    public void setQueueName(final String queueName) {
        this.queueName = queueName;
        if (queueName != null) {
            this.putQueryParameter("QueueName", queueName);
        }
    }

    public void setResourceOwnerId(final Long resourceOwnerId) {
        this.resourceOwnerId = resourceOwnerId;
        if (resourceOwnerId != null) {
            this.putQueryParameter("ResourceOwnerId", resourceOwnerId.toString());
        }
    }

    public void setOwnerId(final Long ownerId) {
        this.ownerId = ownerId;
        if (ownerId != null) {
            this.putQueryParameter("OwnerId", ownerId.toString());
        }
    }

    @Override
    public Class<QueryTokenForMnsQueueResponse> getResponseClass() {
        return QueryTokenForMnsQueueResponse.class;
    }
}
