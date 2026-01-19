package org.jarApiAutomation.endpoints;

public class AutopayEndpoints {
    private AutopayEndpoints() {
        // private constructor to prevent instantiation
    }

    public static final String INITIATE_AUTOPAY = "/api/autopay/initiate"; // initiate api
    public static final String AUTOPAY_STATUS = "/api/autopay/status"; // status api
}
