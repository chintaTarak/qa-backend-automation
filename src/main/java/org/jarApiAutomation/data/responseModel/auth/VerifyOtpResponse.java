package org.jarApiAutomation.data.responseModel.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyOtpResponse extends CommonResultModel {

    private VerifyOtpResponseData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifyOtpResponseData {
        private String accessToken;
        private String refreshToken;
        private String sessionId;
        private long tokenExpiryTime;
    }
}
