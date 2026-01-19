package org.jarApiAutomation.data.responseModel.auth.responseBuyGold;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyGoldResponse {

    private boolean success;
    private DataResponse data;

    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataResponse {
        private String paymentProvider;
        private String paymentInitiateType;
        private Object paytm;
        private Object jusPay;
        private Object phonepe;
        private Object razorpay;
        private MockServerManualPayment mockServerManualPayment;
        private double txnAmount;
        private String orderId;
        private Object priceResponse;
        private String transactionType;
        private Object juspayScreenExtraGoldResp;
        private boolean retryAllowed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MockServerManualPayment {
        private String merchantId;
        private String merchantOrderId;
        private double amount;
        private String merchantUserId;
        private String mobileNumber;
        private String paymentStatus;
    }
}
