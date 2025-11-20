package org.jarApiAutomation.data.responseModel.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecryptOtpResponse {
    private boolean success;
    private String otp;
    private String errorMessage;
}
