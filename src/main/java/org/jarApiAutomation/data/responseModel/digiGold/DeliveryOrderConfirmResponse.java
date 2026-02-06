package org.jarApiAutomation.data.responseModel.digiGold;

import lombok.Data;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
public class DeliveryOrderConfirmResponse  extends CommonResultModel
{

    private boolean success;
    private DataPayload data;

    @Data
    public static class DataPayload
    {
        private String orderId;
        private String userId;
        private String status;
        private String invoiceId;
        private String invoiceStatus;
    }
}
