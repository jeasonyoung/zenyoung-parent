package com.alicom.mns.tools;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.http.ClientConfiguration;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dybaseapi.model.v20170525.QueryTokenForMnsQueueRequest;
import com.aliyuncs.dybaseapi.model.v20170525.QueryTokenForMnsQueueResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.HttpClientConfig;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * TokenGetterForAlicom
 *
 * @author aliyun
 */
@Slf4j
@RequiredArgsConstructor
public class TokenGetterForAlicom {
    private final String accessKeyId;
    private final String accessKeySecret;
    private String regionId = "cn-hangzhou";
    private String mnsAccountEndpoint;

    @Getter
    @Setter
    private Long ownerId;
    @Getter
    @Setter
    private HttpClientConfig httpClientConfig;
    @Getter
    @Setter
    private ClientConfiguration mnsClientConfiguration;

    private IAcsClient iAcsClient;
    private static final long BUFFER_TIME = 120000L;
    private final Object lock = new Object();
    private final Map<String, TokenForAlicom> tokenMap = Maps.newConcurrentMap();

    protected void init(final RegionEnum regionEnum) {
        if (Objects.nonNull(regionEnum)) {
            this.regionId = regionEnum.getRegionId();
        }
        final IClientProfile profile = DefaultProfile.getProfile(this.regionId, this.accessKeyId, this.accessKeySecret);
        if (Objects.nonNull(this.httpClientConfig)) {
            profile.setHttpClientConfig(this.httpClientConfig);
        }
        this.mnsAccountEndpoint = String.format("https://1943695596114318.mns.%s.aliyuncs.com/", this.regionId);
        this.iAcsClient = new DefaultAcsClient(profile);
    }

    @SuppressWarnings({"all"})
    protected void initForVpc(final String regionId) throws ClientException {
        this.mnsAccountEndpoint = String.format("http://1943695596114318.mns.%s-internal-vpc.aliyuncs.com", regionId);
        this.regionId = regionId;
        final String domainForVpc = String.format("dybaseapi-vpc.%s.aliyuncs.com", regionId);
        DefaultProfile.addEndpoint(regionId, regionId, "Dybaseapi", domainForVpc);
        final IClientProfile profile = DefaultProfile.getProfile(regionId, this.accessKeyId, this.accessKeySecret);
        if (Objects.nonNull(this.httpClientConfig)) {
            profile.setHttpClientConfig(this.httpClientConfig);
        }
        this.iAcsClient = new DefaultAcsClient(profile);
    }

    public TokenForAlicom getTokenByMessageType(final String messageType, final String queueName) throws ClientException, ParseException {
        TokenForAlicom token = this.tokenMap.get(messageType);
        final Long now = System.currentTimeMillis();
        if (token == null || token.getExpireTime() - now < BUFFER_TIME) {
            synchronized (this.lock) {
                token = this.tokenMap.get(messageType);
                if (token == null || token.getExpireTime() - now < BUFFER_TIME) {
                    TokenForAlicom oldToken = null;
                    if (token != null) {
                        oldToken = token;
                    }
                    token = this.getTokenFromRemote(messageType, queueName);
                    CloudAccount account;
                    if (Objects.isNull(this.mnsClientConfiguration)) {
                        account = new CloudAccount(token.getTempAccessKeyId(), token.getTempAccessKeySecret(), this.mnsAccountEndpoint, token.getToken());
                    } else {
                        account = new CloudAccount(token.getTempAccessKeyId(), token.getTempAccessKeySecret(), this.mnsAccountEndpoint, token.getToken(),
                                null, this.mnsClientConfiguration);
                    }
                    final MNSClient client = account.getMNSClient();
                    final CloudQueue queue = client.getQueueRef(queueName);
                    token.setClient(client);
                    token.setQueue(queue);
                    this.tokenMap.put(messageType, token);
                    if (oldToken != null) {
                        oldToken.closeClient();
                    }
                }
            }
        }
        return token;
    }

    @SuppressWarnings({"all"})
    private TokenForAlicom getTokenFromRemote(final String messageType, final String queueName) throws ClientException, ParseException {
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        final QueryTokenForMnsQueueRequest request = new QueryTokenForMnsQueueRequest();
        request.setAcceptFormat(FormatType.JSON);
        request.setMessageType(messageType);
        request.setSysProtocol(ProtocolType.HTTPS);
        request.setProtocol(ProtocolType.HTTPS);
        request.setSysMethod(MethodType.POST);
        request.setMethod(MethodType.POST);
        if (StringUtils.isNotBlank(queueName)) {
            request.setQueueName(queueName);
        }
        if (Objects.nonNull(this.ownerId)) {
            request.setOwnerId(this.ownerId);
        }
        final QueryTokenForMnsQueueResponse response = (QueryTokenForMnsQueueResponse) this.iAcsClient.getAcsResponse(request);
        final String resultCode = response.getCode();
        if (Objects.nonNull(resultCode) && "OK".equals(resultCode)) {
            final QueryTokenForMnsQueueResponse.MessageTokenDTO dto = response.getMessageTokenDTO();
            final TokenForAlicom token = new TokenForAlicom();
            final String timeStr = dto.getExpireTime();
            token.setMessageType(messageType);
            token.setExpireTime(df.parse(timeStr).getTime());
            token.setToken(dto.getSecurityToken());
            token.setTempAccessKeyId(dto.getAccessKeyId());
            token.setTempAccessKeySecret(dto.getAccessKeySecret());
            return token;
        } else {
            log.error("getTokenFromRemote_error,messageType:" + messageType + ",code:" + response.getCode() + ",message:" + response.getMessage());
            throw new ServerException(response.getCode(), response.getMessage());
        }
    }
}
