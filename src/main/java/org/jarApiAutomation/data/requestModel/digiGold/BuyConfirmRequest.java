package org.jarApiAutomation.data.requestModel.digiGold;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuyConfirmRequest
{
    private String userId;
    private String orderId;
    private String code;
    private Boolean isSync;

    public static BuyConfirmRequest buyConfirmPayload(
            String userId,
            String orderId,
            String code,
            Boolean isSync
    ) {
        return BuyConfirmRequest.builder()
                .userId(userId)
                .orderId(orderId)
                .code(code)
                .isSync(isSync)
                .build();
    }
}
