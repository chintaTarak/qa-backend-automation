package org.jarApiAutomation.data.requestModel.digiGold;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryOrderConfirmRequest
{
    private String orderId;
    private String userId;
    private Boolean isSync;

    /**
     * Static factory method for creating DeliveryOrderConfirmRequest
     */
    public static DeliveryOrderConfirmRequest createDeliveryOrderConfirm(
            String orderId,
            String userId,
            Boolean isSync
    )
    {
        return DeliveryOrderConfirmRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .isSync(isSync)
                .build();
    }
}
