package org.jarApiAutomation.data.requestModel.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOtpRequest {
    private String countryCode;
    private String phoneNumber;
    private String otp;
    private String reqId;
    private boolean logoutFromOtherDevices;
    private DeviceDetails deviceDetails;

    @Data
    public static class DeviceDetails {
        private String advertisingId;
    }

    public static VerifyOtpRequest verifyOtpPayload(
            String countryCode, String phoneNumber, String otp, String reqId) {
        return VerifyOtpRequest.builder()
                .countryCode(countryCode)
                .phoneNumber(phoneNumber)
                .otp(otp)
                .reqId(reqId)
                .build();
    }
}
