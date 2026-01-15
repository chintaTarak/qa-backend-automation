package org.jarApiAutomation.data.requestModel.digiGold;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellConfirmRequest {
    private String userId;
    private String orderId;
    private String code;
    private Boolean isSync;

    public static SellConfirmRequest sellConfirmRequest(
            String userId, String orderId, String code, Boolean isSync) {
        return SellConfirmRequest.builder()
                .userId(userId)
                .orderId(orderId)
                .code(code)
                .isSync(isSync)
                .build();
    }
}
