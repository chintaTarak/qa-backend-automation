package org.jarApiAutomation.data.responseModel.goldSDK;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateKycResponse extends CommonResultModel {

    private DataObj data;

    @Data
    public static class DataObj {
        private String userId;
        private String verificationStatus; // PENDING
        private String submittedAt;
    }
}

