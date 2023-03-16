package com.huawei.services.runtime.entity.lts;

import lombok.Data;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * LTS Body
 *
 * @author young
 */
@Data
public class LtsBody {
    private String data;

    public String getRawData() throws UnsupportedEncodingException {
        final byte[] decoded = Base64.decodeBase64(this.data);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
