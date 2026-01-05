package org.jarApiAutomation.data.responseModel.digiGold;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@EqualsAndHashCode(callSuper = true)
@Data
public class BuyConfirmResponse extends CommonResultModel {
    private boolean success;
    private DataResult data;

    @Data
    public static class DataResult {
        private String orderId;
        private String status;
        private String invoiceId;
    }
}
