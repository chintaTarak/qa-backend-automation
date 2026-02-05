package org.jarApiAutomation.data.responseModel.digiGold;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SellConfirmResponse extends CommonResultModel {
    private SellConfirmResponseData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SellConfirmResponseData {
        private String invoiceId;
        private String orderId;
        private String status;
    }
}
