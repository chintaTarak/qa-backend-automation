package org.jarApiAutomation.data.responseModel.digiGold;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SellPriceResponse extends CommonResultModel {
    private DataObj data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataObj {
        private String id;
        private String code;
        private BigDecimal assetPrice;
        private String rateStatus;
    }
}
