package org.jarApiAutomation.data.responseModel.digiGold;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = true)
public class BuyStatusResponse extends CommonResultModel {
    private boolean success;
    private DataResult data;

    @Data
    public static class DataResult {
        private String orderId;
        private String userId;
        private String status;
        private String invoiceStatus;
        private String invoiceId;
    }
}
