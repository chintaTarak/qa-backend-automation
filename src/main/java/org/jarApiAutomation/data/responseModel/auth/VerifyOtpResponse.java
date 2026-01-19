package org.jarApiAutomation.data.responseModel.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
        private User user;
        private long tokenExpiryTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String firstName;
        private String lastName;
        private int age;
        private String phoneNumber;
        private boolean onboarded;
        private long createdAt;
        private String userId;
        private boolean userGoalSetup;
        private boolean nameFetchedFromVpa;
    }
}
