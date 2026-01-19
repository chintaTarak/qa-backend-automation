package org.jarApiAutomation.data.requestModel.autopay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusAutopayRequest {

    private MockServer mockServer;
    private String upiApp;
    private String subscriptionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MockServer {
        private String authReqId;
    }

    // Static factory method (for cleaner test code)
    public static StatusAutopayRequest build(
            String subscriptionId, String upiApp, String authReqId) {
        MockServer mockServer = new MockServer(authReqId);
        return new StatusAutopayRequest(mockServer, upiApp, subscriptionId);
    }
}
