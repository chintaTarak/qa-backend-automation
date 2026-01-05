package org.jarApiAutomation.data.responseModel.digiGold;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@EqualsAndHashCode(callSuper = true)
@Data
public class BuyPriceResponse extends CommonResultModel {

    private boolean success;
    private DataObject data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataObject {
        private String id;
        private String code;
        private BigDecimal assetPrice;
        private List<Tax> applicableTaxes;
        private String currentTime;
        private String expiryTime;
        private String rateStatus;
        private String rateRuleId;
    }

    @Data
    public static class Tax {
        private String name;
        private BigDecimal value;
        private String type;
    }
}
