package org.jarApiAutomation.data.requestModel.goldSDK;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoPayInitiateRequest {

    private String frequency;
    private int amount;
    private int maxAmount;
    private String packageName;

    public static AutoPayInitiateRequest initiateAutoPayRequest(
            String frequency,
            int amount,
            int maxAmount,
            String packageName
    ) {
        return AutoPayInitiateRequest.builder()
                .frequency(frequency)
                .amount(amount)
                .maxAmount(maxAmount)
                .packageName(packageName)
                .build();
    }
}
