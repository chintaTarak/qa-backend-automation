package org.jarApiAutomation.data.responseModel.goldSDK;

import lombok.Data;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KycStatusResponse extends CommonResultModel {

    private DataObj data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataObj {
        private String userId;
        private List<KycDocumentDetails> kycDocumentDetails;
        private boolean panKycVerified;
        private boolean kycVerified;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KycDocumentDetails {
        private String name;
        private String kycDocType;
        private String docNumber;
        private String verificationStatus;
        private String failureReason;
        private String verifiedOn;
    }
}
