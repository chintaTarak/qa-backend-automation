package org.jarApiAutomation.data.requestModel.digiGold;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SellVerifyRequest {
    private String rateId;
    private String userId;
    private BigDecimal volume;
    private BigDecimal amount;
    private String  code;
    private String merchantOrderId;
    private String calculationType;

    public static SellVerifyRequest sellVerifyRequest(String rateId, String userId, BigDecimal volume,
                                               BigDecimal amount, String code, String merchantOrderId, String calculationType) {
        return SellVerifyRequest.builder()
                .rateId(rateId)
                .userId(userId)
                .volume(volume)
                .amount(amount)
                .code(code)
                .merchantOrderId(merchantOrderId)
                .calculationType(calculationType)
                .build();
    }
}


