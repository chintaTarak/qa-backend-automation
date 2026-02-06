package org.jarApiAutomation.data.requestModel.goldSDK;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitiateAadhaarKycRequest implements BaseKycRequest {
    private String phoneNumber;
    private String countryCode;
    private kycVerificationDoc kycVerificationDoc;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class kycVerificationDoc {
        private String kycDocType;
        private String docNumber;
        private String docFrontImageId;
        private String name;
        private String dob;
    }
}
