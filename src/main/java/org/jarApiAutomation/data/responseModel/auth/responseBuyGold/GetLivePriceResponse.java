package org.jarApiAutomation.data.responseModel.auth.responseBuyGold;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetLivePriceResponse {

    private boolean success;
    private DataNode data;
    private String errorMessage;
    private String errorCode;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataNode {

        private double price;
        private int validity;
        private String rateId;
        private double applicableTax;
        private String rateValidity;

        @JsonProperty("isPriceDrop")
        private boolean priceDrop;

        private List<TaxItem> taxItems;
        private String vendorType;
        private Object rateRuleId;
        private Object feeConfig;
        private int volumeCalculationScale;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaxItem {

        private String name;
        private double value;
        private String type;
    }
}
