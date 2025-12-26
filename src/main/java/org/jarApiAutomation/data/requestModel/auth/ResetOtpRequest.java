package org.jarApiAutomation.data.requestModel.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetOtpRequest {
    private String mobileNumber;
}
