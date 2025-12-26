package org.jarApiAutomation.data.responseModel.digiGold;

import lombok.Data;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
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
