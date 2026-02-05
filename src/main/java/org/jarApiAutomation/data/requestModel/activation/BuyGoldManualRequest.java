package org.jarApiAutomation.data.requestModel.activation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyGoldManualRequest {

    private BigDecimal amount;
    private BigDecimal volume;
    private PriceResponse priceResponse;
    private String requestType;
    private String paymentProvider;
    private double jarWinningsUsedAmount;
    private String flowType;
    private String paymentExperimentType;
    private String mockServerTransactionStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceResponse {
        private BigDecimal price;
        private double applicableTax;
        private String rateId;
        private String rateValidity;
        private boolean isPriceDrop;
        private int validity;
    }

    public static BuyGoldManualRequest BugGoldManualRequestPayload(
            BigDecimal amount,
            BigDecimal volume,
            BigDecimal price,
            String rateId,
            String rateValidity,
            int validity,
            String mockServerTransactionStatus) {

        return BuyGoldManualRequest.builder()
                .amount(amount)
                .volume(volume)
                .priceResponse(
                        BuyGoldManualRequest.PriceResponse.builder()
                                .price(price)
                                .applicableTax(3.0)
                                .rateId(rateId)
                                .rateValidity(rateValidity)
                                .isPriceDrop(false)
                                .validity(validity)
                                .build())
                .requestType("AMOUNT")
                .paymentProvider("MOCK_SERVER")
                .jarWinningsUsedAmount(0.0)
                .flowType("BUY_GOLD_SCREEN")
                .paymentExperimentType("Non_bottomSheet_payment")
                .mockServerTransactionStatus(mockServerTransactionStatus)
                .build();
    }
}
