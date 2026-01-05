package org.jarApiAutomation.data.responseModel.digiGold;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SellVerifyResponse extends CommonResultModel {
    private SellVerifyResponseData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SellVerifyResponseData {
        private String orderId;
        private String rateId;
        private String code;
        private BigDecimal amount;
        private String userId;
        private int volume;
        private BigDecimal rate;
        private String rateRuleId;
    }
}
