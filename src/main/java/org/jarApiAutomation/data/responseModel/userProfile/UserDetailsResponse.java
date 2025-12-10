package org.jarApiAutomation.data.responseModel.userProfile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailsResponse extends CommonResultModel {
    private UserDetailsResponseData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserDetailsResponseData {
        private String phoneNumber;
        private String firstName;
        private boolean kycVerified;
        private String userId;
        private boolean onboarded;
    }
}
