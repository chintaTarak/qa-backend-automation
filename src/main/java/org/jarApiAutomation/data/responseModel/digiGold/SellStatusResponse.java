package org.jarApiAutomation.data.responseModel.digiGold;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SellStatusResponse extends CommonResultModel {
    private SellStatusResponseData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SellStatusResponseData {
        private String orderId;
        private String userId;
        private String status;
        private String invoiceStatus;
        private String invoiceId;
    }
}
