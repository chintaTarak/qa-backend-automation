package org.jarApiAutomation.data.requestModel.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOtpRequest {

    private String otp;
    private String phoneNumber;
    private String reqId;

    public static VerifyOtpRequest verifyPayload(String otp,String phoneNumber,String reqId ){
      return VerifyOtpRequest.builder()
              .phoneNumber(phoneNumber).
              otp(otp).
              reqId(reqId)
              .build();
    }


}
