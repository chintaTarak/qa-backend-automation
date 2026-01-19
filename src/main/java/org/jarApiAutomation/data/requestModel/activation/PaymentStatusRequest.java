package org.jarApiAutomation.data.requestModel.activation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentStatusRequest {

    private String paymentProvider;
    private String orderId;
    private String transactionType;
    private String buyGoldFlowContext;

    public static PaymentStatusRequest PaymentStatusRequestPayload(
            String paymentProvider,
            String orderId,
            String transactionType,
            String buyGoldFlowContext) {
        return PaymentStatusRequest.builder()
                .paymentProvider(paymentProvider)
                .orderId(orderId)
                .transactionType(transactionType)
                .buyGoldFlowContext(buyGoldFlowContext)
                .build();
    }
}
