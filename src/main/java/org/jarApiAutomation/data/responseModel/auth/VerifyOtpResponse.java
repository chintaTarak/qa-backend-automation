package org.jarApiAutomation.data.responseModel.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyOtpResponse extends CommonResultModel {

    private VerifyOtpResponseData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifyOtpResponseData {
        private String accessToken;
        private String refreshToken;
        private String sessionId;
        private long tokenExpiryTime;
    }
}
