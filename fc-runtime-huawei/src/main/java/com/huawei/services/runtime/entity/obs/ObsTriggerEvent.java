package com.huawei.services.runtime.entity.obs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * Obs触发事件
 *
 * @author young
 */
@Data
public class ObsTriggerEvent implements ObsTriggerInfo {
    @JsonProperty("Records")
    private ObsRecord[] record;

    private Optional<ObsRecord[]> check() {
        if (this.record != null && this.record.length >= 1) {
            if (this.record.length > 1) {
                throw new IllegalArgumentException("Record's length is to long! ");
            } else {
                return Optional.of(this.record);
            }
        } else {
            throw new IllegalArgumentException("Record can't be null. ");
        }
    }

    @Override
    public String getBucketName() {
        final Optional<ObsRecord[]> obsRecord = this.check();
        return obsRecord.map(r -> r[0])
                .map(ObsRecord::getObs)
                .map(ObsBody::getBucket)
                .map(Bucket::getName)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getObjectKey() {
        final Optional<ObsRecord[]> obsRecord = this.check();
        return obsRecord.map(r -> r[0])
                .map(ObsRecord::getObs)
                .map(ObsBody::getObsObject)
                .map(ObsObject::getKey)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String getEventName() {
        final Optional<ObsRecord[]> obsRecord = this.check();
        return obsRecord.map(r -> r[0])
                .map(ObsRecord::getEventName)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Data
    public static class ObsRecord {
        private String eventVersion;
        private String eventSource;
        private String eventRegion;
        private String eventTime;
        private String eventName;
        private IdEntity userIdentity;
        private RequestParameters requestParameters;
        private Map<String, Object> responseElements;
        private ObsBody obs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdEntity {
        @JsonProperty("ID")
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestParameters {
        @JsonProperty("sourceIPAddress")
        private String sourceIpAddress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObsBody {
        @JsonProperty("Version")
        private String version;
        private String configurationId;
        private Bucket bucket;
        @JsonProperty("object")
        private ObsObject obsObject;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bucket {
        private String name;
        private String bucket;
        @JsonProperty("ownerIdentity")
        private IdEntity idEntity;
    }
}
