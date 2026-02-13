package org.jarApiAutomation.data.responseModel.goldSDK;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class UserAuthResponse extends CommonResultModel {
    private boolean success;
    private DataObj data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataObj {
        private String accessToken;
        private String refreshToken;
        private long expiryTime;
    }
}
