package org.jarApiAutomation.data.requestModel.autopay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateAutopayRequest {

    private String provider;
    private double mandateAmount;
    private String authWorkflowType;
    private String packageName;
    private String phonePeVersionCode;
    private String subscriptionType;
    private String subsSetupType;
    private String mandateSetupFrom;
    private String mockServerTransactionStatus; // SUCCESS / PENDING / FAILED

    /**
     * Builder method to create a valid InitiateAutopayRequest This keeps test code clean and avoids
     * duplication
     */
    public static InitiateAutopayRequest buildRequest(
            String provider,
            double mandateAmount,
            String authWorkflowType,
            String packageName,
            String phonePeVersionCode,
            String subscriptionType,
            String subsSetupType,
            String mandateSetupFrom,
            String mockServerTransactionStatus) {
        InitiateAutopayRequest request = new InitiateAutopayRequest();
        request.setProvider(provider);
        request.setMandateAmount(mandateAmount);
        request.setAuthWorkflowType(authWorkflowType);
        request.setPackageName(packageName);
        request.setPhonePeVersionCode(phonePeVersionCode);
        request.setSubscriptionType(subscriptionType);
        request.setSubsSetupType(subsSetupType);
        request.setMandateSetupFrom(mandateSetupFrom);
        request.setMockServerTransactionStatus(mockServerTransactionStatus);
        return request;
    }
}
