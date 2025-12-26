package org.jarApiAutomation.data.responseModel.digiGold;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyVerifyResponse extends CommonResultModel {
    private boolean success;
    private DataObj data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataObj {
        private String orderId;
        private String rateId;
        private String code;
        private BigDecimal amount;
        private BigDecimal rate;
        private BigDecimal volume;
        private BigDecimal preTaxAmount;
        private BigDecimal taxAmount;
        private String userId;
        private String rateRuleId;
    }
}
