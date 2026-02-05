package org.jarApiAutomation.data.responseModel.autopay;

import lombok.Data;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
public class InitiateAutopayResponse extends CommonResultModel {

    private AutopayData data;

    @Data
    public static class AutopayData {
        private String id;
        private double txnAmount;
        private MockServer mockServer;
        private String provider;

        @Data
        public static class MockServer {
            private String id;
            private double txnAmount;
            private String redirectType; // only for SUCCESS
            private String redirectUrl; // only for SUCCESS
            private String authReqId; // only for SUCCESS
        }
    }
}
