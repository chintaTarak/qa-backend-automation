package org.jarApiAutomation.data.requestModel.goldSDK;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitiatePanKycRequest implements BaseKycRequest {

    private String phoneNumber;
    private String countryCode;
    private panVerificationDoc panVerificationDoc;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class panVerificationDoc {
        private String kycDocType;
        private String docNumber;
        private String docFrontImageId;   // from upload API
        private String name;
        private String dob;
    }
}

